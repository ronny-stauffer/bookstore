package org.books.business.catalog;

import com.amazon.webservices.*;
import java.math.BigInteger;
import java.util.List;
import org.books.common.exception.CatalogException;

/**
 *
 * @author Christoph Horber
 */
class BookSearch {
    
    private final String title;
    private final String author;
    private final String publisher;
    private final int maxResults;
    
    BookSearch(String title, String author, String publisher, int maxResults) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.maxResults = maxResults;
    }
    
    int readTotalPages() throws CatalogException {
        List<Items> responses = search(false, 1, 1);
        return getTotalPages(responses);
    }
    
    int getTotalPages(List<Items> responses) {
        if (responses == null || responses.size() < 1) {
            return 0;
        }
        
        Items response = responses.get(0);
        BigInteger totalPages = response.getTotalPages();
        return totalPages == null ? 0 : totalPages.intValue();
    }
    
    private List<Items> search(ItemSearch searchRequest) throws CatalogException {
        AWSECommerceService amazonService = new AWSECommerceService();
        AWSECommerceServicePortType amazonServicePort = amazonService.getAWSECommerceServicePort();
        
        ItemSearchResponse response = amazonServicePort.itemSearch(searchRequest);

        // Exception handling
        if (response.getOperationRequest() != null) {
            ErrorHandler.handle(response.getOperationRequest());
        }
        
        return response.getItems(); // can be null
    }
    
    List<Items> search(int itemPage, int count) throws CatalogException {
        // response with attributes
        return search(true, itemPage, count);
    }
    
    private List<Items> search(boolean includeAttributes, int itemPage, int count) throws CatalogException {
        ItemSearch searchRequest = createSearchRequest(includeAttributes, itemPage, count);
        return search(searchRequest);
    }
    
    private ItemSearch createSearchRequest(boolean includeAttributes, int itemPage, int count) {
        ItemSearch itemSearch = new ItemSearch();
        itemSearch.setAssociateTag(String.format("%s-%s-%s", title, author, publisher));
        
        for (int i = 0; i < count; i++) {
            ItemSearchRequest itemSearchRequest = createItemSearchRequest(includeAttributes, itemPage + i);
            itemSearch.getRequest().add(itemSearchRequest);
        }
        
        return itemSearch;
    }
        
    private ItemSearchRequest createItemSearchRequest(boolean includeAttributes, int itemPage) {
        ItemSearchRequest itemSearchRequest = new ItemSearchRequest();
        itemSearchRequest.setItemPage(BigInteger.valueOf(itemPage));
        if (includeAttributes) {
            itemSearchRequest.getResponseGroup().add("ItemAttributes");
        }
        itemSearchRequest.setSearchIndex("Books");
        itemSearchRequest.setTitle(title);
        itemSearchRequest.setAuthor(author);
        itemSearchRequest.setPublisher(publisher);
        
        return itemSearchRequest;
    }
}
