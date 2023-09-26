package searchengine.services;

import searchengine.dto.statistics.Response;
import searchengine.dto.statistics.SearchingResponse;

public interface SearchingService {
    Response search(String query, String site, int offset, int limit);
}
