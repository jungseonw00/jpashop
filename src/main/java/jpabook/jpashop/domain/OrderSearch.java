package jpabook.jpashop.domain;

import jpabook.jpashop.domain.order.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearch {
	private String memberName;
	private OrderStatus orderStatus;
}
