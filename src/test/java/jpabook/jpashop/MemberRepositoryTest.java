//package jpabook.jpashop;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@Slf4j
//class MemberRepositoryTest {
//
//    @Autowired MemberRepository memberRepository;
//
//    @Test
//    @Transactional // EntityManager를 통한 데이터 변경은 Transaction 안에서 수행되어야 한다.
//    public void testMember() throws Exception {
//        // given
//        Member member = new Member();
//        member.setUsername("memberA");
//
//        // when
//        Long saveId = memberRepository.save(member);
//        Member findMember = memberRepository.find(saveId);
//
//        // then
//        assertThat(findMember.getId()).isEqualTo(member.getId());
//        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
//        assertThat(findMember).isEqualTo(member);
//
//        // 같은 트랜잭션안 영속성 컨텍스트에서 식별자가 같으면 같은 엔터티로 취급됨
//        log.info("findMember == member: {}", (findMember == member));
//    }
//}