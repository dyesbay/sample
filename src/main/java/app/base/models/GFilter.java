package app.base.models;

import app.base.db.GEntity;
import app.base.objects.GObject;
import app.base.utils.DateUtils;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class GFilter<ID extends Serializable, Entity extends GEntity<ID>> extends GObject implements Specification<Entity> {

    @ApiParam(defaultValue = "0")
    private Integer page = 0;

    @ApiParam(defaultValue = "10")
    private Integer size = 10;

    @ApiParam(defaultValue = "desc", allowableValues = "asc, desc")
    private String direction = "desc";

    @DateTimeFormat(pattern = DateUtils.SYSTEM_DATE_TIME)
    Date from;

    @DateTimeFormat(pattern = DateUtils.SYSTEM_DATE_TIME)
    Date to;

    public abstract List<Predicate> getPredicates(Root<Entity> root, CriteriaBuilder cb);

    @Override
    public Predicate toPredicate(Root<Entity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return cb.and(getPredicates(root,cb).toArray(new Predicate[0]));
    }

    public PageRequest getPageRequest(){
        return new PageRequest(getPage(),getSize());
    }
}
