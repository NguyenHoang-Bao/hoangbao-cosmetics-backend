package com.hoangbaoshop.hoangbaocosmetics_backend.dto.common;

import lombok.*;

import java.util.List;

//mẫu chung response phân trang
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;     //danh sách dữ liệu trang hiện tại
    private int pageNo;          //trang số mấy (tính từ 0)
    private int pageSize;        //số lượng món mỗi trang
    private long totalElements;  //tổng số lượng trong database
    private int totalPages;      //tổng số trang
    private boolean last;        //phải trang cuối chưa
}