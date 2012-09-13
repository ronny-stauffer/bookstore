package org.books.common.data;

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author Christoph Horber
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
       
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
