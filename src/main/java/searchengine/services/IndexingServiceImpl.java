package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.Error;
import searchengine.dto.statistics.Response;
import searchengine.model.Site;
import searchengine.processors.Indexator;
import searchengine.processors.SiteIndexator;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    Logger logger = Logger.getLogger(IndexingServiceImpl.class.getName());
    @Autowired
    private final Indexator indexator;

    private final SitesList sites;
    private ForkJoinPool pool;


      @Override
        public Response startIndexing() {
            logger.info("startIndexing "+ indexator);
            return indexator.start();
        }

        @Override
        public Response stopIndexing(){
            logger.info("stopIndexing "+ indexator);
            return  indexator.stop();
        }

        @Override
        public Response indexPage(String url) {
            logger.info("indexpage" + indexator);
            return indexator.startPage(url);
        }



}
