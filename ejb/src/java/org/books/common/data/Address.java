package org.books.common.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *
 * @author Christoph Horber
 * @author Ronny Stauffer
 */
@Entity
public class Address extends BaseEntity {
    public static final String EMAIL_ADDRESS_REGEX = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
    @NotNull
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String name;
    
    @Size(max = 100)
    @Column(length = 100)
    private String street;
    
    @NotNull    
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String city;
    
    @NotNull    
    @Pattern(regexp = "\\d{4,6}")
    @Column(nullable = false, length = 6)
    private String zip;
    
    @NotNull    
    @Size(min = 1, max = 30)
    @Column(nullable = false, length = 30)
    private String country;
    
    @NotNull    
    @Size(max = 100)
    @Pattern(regexp = EMAIL_ADDRESS_REGEX)
    @Column(nullable = false, length = 100)
    private String eMailAddress;
    
    public Address() {
        
    }
    
    public Address(Address address) {
        this.name = address.name;
        this.street = address.street;
        this.city = address.city;
        this.zip = address.zip;
        this.country = address.country;
        this.eMailAddress = address.eMailAddress;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
   
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
    
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String geteMailAddress() {
        return eMailAddress;
    }

    public void seteMailAddress(String eMailAddress) {
        this.eMailAddress = eMailAddress;
    }
}