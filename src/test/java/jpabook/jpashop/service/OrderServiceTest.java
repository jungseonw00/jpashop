package jpabook.jpashop.service;

import static jpabook.jpashop.domain.order.OrderStatus.CANCEL;
import static jpabook.jpashop.domain.order.OrderStatus.ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.extend.Item;
import jpabook.jpashop.domain.item.Address;
import jpabook.jpashop.domain.item.extend.Book;
import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTest {

	@Autowired
	EntityManager em;
	@Autowired
	OrderService orderService;
	@Autowired
	OrderRepository orderRepository;

	@Test
	void 상품주문() {
		Member member = createMember();
		em.persist(member);

		Book book = createBook("시골JPA", 10000, 10);
		em.persist(book);

		int orderCount = 2;

		// when
		Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

		// then
		Order getOrder = orderRepository.findOne(orderId);

		assertThat(ORDER).isEqualTo(getOrder.getStatus()).as("상품 주문시 상태는 ORDER");
		assertThat(getOrder.getOrderItems().size()).isEqualTo(1).as("주문한 상품 종류 수가 정확해야 한다.");
		assertThat(getOrder.getTotalPrice()).isEqualTo(10000 * orderCount).as("주문 가격은 가격 * 수량이다.");
		assertThat(book.getStockQuantity()).isEqualTo(8).as("주문 수량만큼 재고가 줄어야 한다.");
	}

	@Test
	void 상품주문_재고수량초과() {
		// given
		Member member = createMember();
		Item item = createBook("시골JPA", 10000, 10);

		int orderCount = 11;

		// when
		assertThatThrownBy(() ->
			orderService.order(member.getId(), item.getId(), orderCount))
			.isInstanceOf(NotEnoughStockException.class);

	}

	@Test
	void 주문취소() {
		// given
		Member member = createMember();
		Book item = createBook("시골 JPA", 10000, 10);

		int orderCount = 2;

		Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

		// when
		orderService.cancelOrder(orderId);

		// then
		Order getOrder = orderRepository.findOne(orderId);

		assertThat(getOrder.getStatus()).isEqualTo(CANCEL);
		assertThat(item.getStockQuantity()).isEqualTo(10);

//		assertThat();
	}

	private Book createBook(String name, int price, int stock) {
		Book book = new Book();
		book.setName(name);
		book.setPrice(price);
		book.setStockQuantity(stock);
		em.persist(book);
		return book;
	}

	private Member createMember() {
		Member member = new Member();
		member.setName("회원1");
		member.setAddress(new Address("서울", "경기", "123-123"));
		em.persist(member);
		return member;
	}

}