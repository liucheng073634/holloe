package com.atguigu.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AddressVo {


    MemberAddressVo address;

    BigDecimal fare;
}
