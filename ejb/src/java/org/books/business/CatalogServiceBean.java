package org.books.business;

import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.books.business.catalog.Catalog;
import org.books.business.catalog.CatalogImpl;
import org.books.common.data.Book;
import org.books.common.exception.CatalogException;
import org.books.common.exception.InvalidCriteriaException;

/**
 *
 * @author Christoph Horber
 */
@Stateless(name = "CatalogService")
public class CatalogServiceBean implements CatalogServiceRemote, CatalogServiceLocal {

    private static final String WILDCARD = "%";
    
    private final Catalog catalog = new CatalogImpl();
    
    @Resource(name = "maxSearchResults") int maxSearchResults;
    
    @PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = "bookstore")
    private EntityManager em;
    
    @Override
    public Book addBook(Book book) {
        em.persist(book);
        return book;
    }

    @Override
    public List<Book> searchBooks(String title, String author, String publisher) throws InvalidCriteriaException {
        // at least one criteria must be set
        if ((title == null || title.isEmpty())
                && (author == null || author.isEmpty())
                && (publisher == null || publisher.isEmpty())) {
            throw new InvalidCriteriaException();
        }
        
        /*
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> q = cb.createQuery(Book.class);
        Root<Book> b = q.from(Book.class);
        
        q.select(b);
        
        Predicate p = null;
        ParameterExpression<String> paramTitle = cb.parameter(String.class);
        ParameterExpression<String> paramAuthors = cb.parameter(String.class);
        ParameterExpression<String> paramPublisher = cb.parameter(String.class);
        
        if (title != null) {
            Predicate pTitle = cb.like(cb.lower(b.<String>get("title")), cb.lower(paramTitle));
            p = and(cb, p, pTitle);
        }
        if (author != null) {
            Predicate pAuthor = cb.like(cb.lower(b.<String>get("authors")), cb.lower(paramAuthors));
            p = and(cb, p, pAuthor);
        }
        if (publisher != null) {
            Predicate pPublisher = cb.like(cb.lower(b.<String>get("publisher")), cb.lower(paramPublisher));
            p = and(cb, p, pPublisher);
        }
        
        q.where(p);
        TypedQuery<Book> query = em.createQuery(q);
        
        if (title != null) {
            query.setParameter(paramTitle, wildcard(title));
        }
        if (author != null) {
            query.setParameter(paramAuthors, wildcard(author));
        }
        if (publisher != null) {
            query.setParameter(paramPublisher, wildcard(publisher));
        }
        
        return query.getResultList();
        */
        
        List<Book> books;

        try {
            books = catalog.searchBooks(title, author, publisher, maxSearchResults);
        } catch (CatalogException e) {
            throw new EJBException(e);
        }
        
        return books;
    }
    
    @Override
    public Book findBook(String isbn) {
        Query q = em.createNamedQuery("findBook");
        q.setParameter("isbn", isbn);
        List<Book> books = q.getResultList();
        
        if (books == null || books.isEmpty()) {
            return null;
        }
        
        return books.get(0);
    }

    @Override
    public Book updateBook(Book book) {
        return em.merge(book);
    }

    @Override
    public void removeBook(Book book) {
        Book mergedBook = em.merge(book);
        em.remove(mergedBook);
    }

    private static Predicate and(CriteriaBuilder cb, Predicate previous, Predicate currentPredicate) {
        if (previous == null) {
            return currentPredicate;
        }
        return cb.and(previous, currentPredicate);
    }
    
    private static String wildcard(String param) {
        return WILDCARD + param + WILDCARD;
    }
}
