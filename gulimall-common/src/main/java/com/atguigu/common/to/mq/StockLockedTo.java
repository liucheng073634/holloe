package com.atguigu.common.to.mq;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StockLockedTo implements Serializable {
    private Long id;
    private StockDetailTo detail;
}
