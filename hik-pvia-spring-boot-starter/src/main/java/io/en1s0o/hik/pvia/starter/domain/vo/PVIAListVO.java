package io.en1s0o.hik.pvia.starter.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * PVIA 列表 VO
 *
 * @param <T> 数据类型
 * @author En1s0o
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class PVIAListVO<T> extends PVIABaseVO<List<T>> {

}
