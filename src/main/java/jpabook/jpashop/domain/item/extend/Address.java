package jpabook.jpashop.domain.item.extend;

import jakarta.persistence.Embeddable;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;

@Embeddable
@Getter
public class Address extends Item {
    private String city;
    private String street;
    private String zipcode;

    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
