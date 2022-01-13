package org.zerock.guestbook.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.zerock.guestbook.dto.GuestbookDTO;
import org.zerock.guestbook.dto.PageRequestDTO;
import org.zerock.guestbook.dto.PageResultDTO;
import org.zerock.guestbook.entity.Guestbook;
import org.zerock.guestbook.entity.QGuestbook;
import org.zerock.guestbook.repository.GuestbookRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.function.Function;

@Service
@Log4j2
public class GuestbookServiceImpl implements GuestbookService{

    private final GuestbookRepository repository;

    public GuestbookServiceImpl(GuestbookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Long register(GuestbookDTO dto) {
        log.info("DTO-------------------");
        log.info(dto);

        Guestbook entity = dtoToEntity(dto);

        log.info(entity);

        repository.save(entity);

        return entity.getGno();
    }

    @Override
    public PageResultDTO<GuestbookDTO, Guestbook> getList(PageRequestDTO requestDTO) {
        Pageable pageable = requestDTO.getPageable(Sort.by("gno").descending());
//        Page<Guestbook> result = repository.findAll(pageable);
//
//        Function<Guestbook,GuestbookDTO> fn = (entity -> entityToDto(entity));
        BooleanBuilder booleanBuilder = getSearch(requestDTO);

        Page<Guestbook> result = repository.findAll(booleanBuilder,pageable); // querydsl 사용
        Function<Guestbook, GuestbookDTO> fn = (entity -> entityToDto(entity));

        return new PageResultDTO<>(result,fn);
    }

    /* 방명록 읽기 처리 기능 */
    @Override
    public GuestbookDTO read(Long gno) {
        Optional<Guestbook> result = repository.findById(gno);

        return result.isPresent()? entityToDto(result.get()) : null;
    }

    /* 방명록 삭제 기능 */
    @Override
    public void remove(Long gno) {
        repository.deleteById(gno);
    }

    /* 방명록 수정 기능 */
    @Override
    public void modify(GuestbookDTO dto) {
        Optional<Guestbook> result = repository.findById(dto.getGno());

        if(result.isPresent()){
            Guestbook entity = result.get();

            entity.changeTitle(dto.getTitle());
            entity.changeContent(dto.getContent());

            repository.save(entity);
        }
    }

    /* 검색 기능 */
    private BooleanBuilder getSearch(PageRequestDTO requestDTO) { // Querydsl 처리
        String type = requestDTO.getType();

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        QGuestbook qGuestbook = QGuestbook.guestbook;

        String keyword = requestDTO.getKeyword();

        BooleanExpression expression = qGuestbook.gno.gt(0L); // gno > 0 인것만 생성

        booleanBuilder.and(expression);

        if(type == null || type.trim().length() == 0){
            //검색 조건이 없는 경우
            return booleanBuilder;
        }

        BooleanBuilder conditionBuilder = new BooleanBuilder();

        if(type.contains("t")){
            conditionBuilder.or(qGuestbook.title.contains(keyword));
        }

        if(type.contains("c")){
            conditionBuilder.or(qGuestbook.content.contains(keyword));
        }

        if(type.contains("w")){
            conditionBuilder.or(qGuestbook.writer.containsIgnoreCase(keyword));
        }

        booleanBuilder.and(conditionBuilder);

        return booleanBuilder;
    }
}
