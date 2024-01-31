package jpabook.jpashop.api;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.List;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.item.Address;
import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.order.OrderItem;
import jpabook.jpashop.domain.order.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 접근 권장 순서
 * 1. 엔티티 조회 방식으로 우선 접근 -> 코드를 거의 수정하지않고, 옵션만 약간 변경해서 다양한 성능 최적화를 시도할 수 있음. DTO 조회 방법은 성능 최적화 방식을 변경할 때 많은 코드 변경이 발생함
 * 	1) 페치조인으로 쿼리 수를 최적화
 * 	2) 컬렉션 최적화
 * 		(1) 페이징 필요시 @BatchSize 사용
 * 		(2) 페이징 필요X 페치 조인 사용
 * 2. 엔티티로 조회가 안되면 DTO 조회 방식 사용
 * 3. DTO 조회로 안되면 NativeSQL or Spring JdbcTemplate 사용
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

	private final OrderRepository orderRepository;
	private final OrderQueryRepository orderQueryRepository;

	/**
	 * V1.엔티티 그대로 변환
	 */
	@GetMapping("/api/v1/orders")
	public List<Order> ordersV1() {
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		for (Order order : all) {
			order.getMember().getName();
			order.getDelivery().getAddress();

			List<OrderItem> orderItems = order.getOrderItems();
			orderItems.stream().forEach(o -> o.getItem().getName());
		}
		return all;
	}

	/**
	 * V2.엔티티 조회 후 DTO로 변환
	 */
	@GetMapping("/api/v2/orders")
	public List<OrderDto> ordersV2() {
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<OrderDto> collect = orders.stream()
			.map(OrderDto::new)
			.collect(toList());
		return collect;
	}

	/**
	 * V3.페치 조인으로 쿼리 수 최적화
	 */
	@GetMapping("/api/v3/orders")
	public List<OrderDto> ordersV3() {
		List<Order> orders = orderRepository.findAllWithItem();

		for (Order order : orders) {
			System.out.println("order ref = " + order + " id = " + order.getId());
		}

		List<OrderDto> result = orders.stream()
			.map(OrderDto::new)
			.collect(toList());

		return result;
	}

	/**
	 * V3.1.컬렉션 페이징과 한계 돌파
	 */
	@GetMapping("/api/v3.1/orders")
	public List<OrderDto> ordersV3_page(
		@RequestParam(value = "offset", defaultValue = "0") int offset,
		@RequestParam(value = "limit", defaultValue = "100") int limit) {

		List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

		List<OrderDto> result = orders.stream()
			.map(OrderDto::new)
			.collect(toList());

		return result;
	}

	// ==DTO 직접 조회 시작== //

	/**
	 * V4. DTO 직접 조회(xToMany의 경우 별도로 조회한다.)
	 */
	@GetMapping("/api/v4/orders")
	public List<OrderQueryDto> ordersV4() {
		return orderQueryRepository.findOrderQueryDtos();
	}

	/**
	 * V5. 컬렉션 조회 최적화
	 */
	@GetMapping("/api/v5/orders")
	public List<OrderQueryDto> ordersV5() {
		return orderQueryRepository.findAllByDtoOptimization();
	}

	/**
	 * V6. JOIN 결과 그대로 조회 후 애플리케이션에서 원하는 모양으로 직접 변환
	 *
	 * Query 1번의 장점은 있으나,
	 * 1. 상황에 따라 V5보다 느림
	 * 2. 애플리케이션에서 추가 작업이 필요
	 * 3. 페이징 불가능
	 */
	@GetMapping("/api/v6/orders")
	public List<OrderQueryDto> ordersV6() {
		List<OrderFlatDto> flats = orderQueryRepository.findAllByDtoFlat();

		return flats.stream()
			.collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
				mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
			.entrySet().stream().map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(),e.getValue()))
			.collect(toList());
	}

	@Data
	static class OrderDto {

		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		private List<OrderItemDto> orderItems;

		public OrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getName();
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress();
			orderItems = order.getOrderItems().stream()
				.map(OrderItemDto::new)
				.collect(toList());
		}
	}

	@Getter
	static class OrderItemDto {

		private String itemName;
		private int orderPrice;
		private int count;

		public OrderItemDto(OrderItem orderItem) {
			itemName = orderItem.getItem().getName();
			orderPrice = orderItem.getOrderPrice();
			count = orderItem.getCount();
		}
	}
}
