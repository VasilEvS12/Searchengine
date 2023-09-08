package searchengine.processors;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Logger;


@RequiredArgsConstructor
public class SiteIndexator extends RecursiveAction {
    @Setter
    private Site site;
    @Setter
    private String pageUrl;
    @Setter
    private HashSet<Page> pages;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private Logger logger = Logger.getLogger(SiteIndexator.class.getName());

    private void saveSite() {
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }

    private Document getHtml(String url) throws InterruptedException {
        Document htmlDoc = null;
        String errMessage = "";
        Page page = new Page();

        page.setPath(url);
        page.setSite(site);
        synchronized (pages) {
            if (!pages.add(page)) {
                return htmlDoc;
            }
        }

        Thread.sleep(100);

        try {
            Connection.Response response = Jsoup.connect(url).execute();
            page.setCode(response.statusCode());
            if (response.statusCode() == 200) {
                htmlDoc = Jsoup.connect(url).get();
                page.setContent(htmlDoc.toString());
            }
        } catch (UnsupportedMimeTypeException e) {
            errMessage = url + " - не является страницей";
        } catch (SocketTimeoutException e) {
            errMessage = url + " - время ожидания истекло";
        } catch (IllegalArgumentException e) {
            errMessage = url + " - неверная ссылка";
        } catch (HttpStatusException e) {
            errMessage = url + e.getMessage();
            page.setCode(e.getStatusCode());
        } catch (
                UnknownHostException e) {
            errMessage = url + " - не удается получить доступ к сайту";
        } catch (IOException e) {
            errMessage = url + e.getMessage();
        }

        if (pageUrl == null) {
            site.setLastError(errMessage);
            site.setStatus(Status.FAILED);
        }
        if (!errMessage.equals(""))
            logger.info(url + " Ошибка " + errMessage);
        return htmlDoc;
    }

    private void processPage(String url) throws InterruptedException {
        List<SiteIndexator> pageIndexators = new ArrayList<SiteIndexator>(); // список дочерних процессов
        Document htmlDoc = getHtml(url);
        if (htmlDoc == null) {
            return;
        }
        Elements links = htmlDoc.select("a[abs:href~=^" + htmlDoc.location() + "((?!(#|\\?)).)*$]");
        logger.info(url + " - найдено ссылок: " + links.size());

        links.stream().map(l -> l.absUrl("href"))
                .filter(l -> !htmlDoc.baseUri().equals(l))
                .distinct()
                .forEach(l -> {
                    SiteIndexator indexator = new SiteIndexator(siteRepository, pageRepository);
                    indexator.setSite(site);
                    indexator.setPageUrl(l);
                    indexator.setPages(pages);
                    indexator.fork();
                    pageIndexators.add(indexator);
                });
        logger.info(url + " - потоков запущено " + (long) pageIndexators.size());
        pageIndexators.forEach(SiteIndexator::join);
        if (pageUrl == null) {
            site.setStatus(Status.INDEXED);
        }
    }


    protected void compute() {
        try {
            if (pageUrl == null) {
                logger.info(site.getUrl() + " - запуск индексации");
                Site existSite = siteRepository.findFirstSiteByUrl(site.getUrl());
                if (existSite != null) {
                    site = existSite;
                    logger.info(site.getUrl() + " - удаление данных");
                    pageRepository.deletePagesBySiteId(existSite.getId());
                }
                site.setStatus(Status.INDEXED);
                saveSite();
                processPage(site.getUrl());
                saveSite();
                pageRepository.saveAll(pages.stream().toList());
                logger.info(site.getUrl() + " - индексация завершена");
            } else {
                processPage(pageUrl);
            }
        } catch (InterruptedException | CancellationException e) {
            String  errMessage = site.getUrl() + " - индексация прервана пользователем";
            logger.info(errMessage);
            site.setLastError(errMessage);
            site.setStatus(Status.FAILED);
            saveSite();
        }
    }
}