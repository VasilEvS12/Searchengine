package searchengine.processors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.dto.statistics.Error;
import searchengine.dto.statistics.Response;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

@Getter
@Component
@RequiredArgsConstructor
public class Indexator {
    private final Logger logger = Logger.getLogger(Indexator.class.getName());

    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final PageRepository pageRepository;

    private final SitesList sites;
    private ForkJoinPool pool;

    ForkJoinPool getPoolInstance() {
        if (pool == null || pool.isShutdown()) {
            pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        }
        return pool;
    }


    public Response start() {
        pool = getPoolInstance();

        if (pool.getActiveThreadCount() > 0) {
            return new Error("Индексация уже запущена");
        } else {
            sites.getSites().forEach(s -> {
                Site site = new Site();
                site.setName(s.getName());
                site.setUrl(s.getUrl());
                SiteIndexator siteIndexator = new SiteIndexator(siteRepository,pageRepository);
                siteIndexator.setSite(site);
                siteIndexator.setPages(new HashSet<>());
                pool.execute(siteIndexator);
            });
            return new Response();
        }
    }

    public Response stop() {
        pool = getPoolInstance();
        if (pool.getActiveThreadCount() > 0) {
            pool.shutdownNow();
            return new Response();
        } else {
            return new Error("Индексация не запущена");
        }
    }

    public Response startPage(String url) {
        pool = getPoolInstance();
        if (pool.getActiveThreadCount() > 0) {
            return new Error("Индексация уже запущена");
        } else {
            if (sites.getSites().stream().filter(s -> s.getUrl().equals(url)).count() == 0) {
                return new Error("Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
            }
            sites.getSites()
                    .stream()
                    .filter(s -> s.getUrl().equals(url))
                    .forEach(s -> {
                        Site site = new Site();
                        site.setName(s.getName());
                        site.setUrl(s.getUrl());
                        SiteIndexator siteIndexator = new SiteIndexator(siteRepository,pageRepository);
                        siteIndexator.setSite(site);
                        siteIndexator.setPages(new HashSet<>());
                        pool.execute(siteIndexator);
                    });
            return new Response();
        }
    }
}
