package org.zerock.guestbook;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.zerock.guestbook.entity.Guestbook;
import org.zerock.guestbook.entity.QGuestbook;
import org.zerock.guestbook.repository.GuestbookRepository;

import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
class GuestbookApplicationTests {
	@Autowired
	private GuestbookRepository guestbookRepository;

	@Test
	void contextLoads() {
	}

	@Test
	public void insertDummies(){
		IntStream.range(1,300).forEach(i->{
			Guestbook guestbook = Guestbook.builder()
					.title("Title...."+i)
					.content("Content..."+i)
					.writer("user"+(i%10))
					.build();
			System.out.println(guestbookRepository.save(guestbook));
		});
	}

	@Test
	public void updateTest(){
		Optional<Guestbook> result = guestbookRepository.findById(299L);

		if(result.isPresent()){
			Guestbook guestbook = result.get();

			guestbook.changeTitle("Change Title...");
			guestbook.changeContent("Change Content...");

			guestbookRepository.save(guestbook);
		}
	}

	@Test
	public void testQuery1(){
		Pageable pageable = PageRequest.of(0,10, Sort.by("gno").descending());

		QGuestbook qGuestbook = QGuestbook.guestbook;

		String keyword = "1";

		BooleanBuilder builder = new BooleanBuilder();

		BooleanExpression expression = qGuestbook.title.contains(keyword);

		builder.and(expression);
		
		Page<Guestbook> result = guestbookRepository.findAll(builder,pageable);

		result.stream().forEach(guestbook -> {
			System.out.println(guestbook);
		});
	}
}
