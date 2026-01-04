package com.example.library.service;

import com.example.library.entity.BookStatusHistory;
import com.example.library.mapper.BookStatusHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图书状态历史服务类
 * 
 * @author Library Management System
 */
@Service
@RequiredArgsConstructor
public class BookStatusHistoryService {
    
    private final BookStatusHistoryMapper bookStatusHistoryMapper;
    
    /**
     * 根据馆藏号查询状态历史
     */
    public List<BookStatusHistory> getStatusHistoryByCollectionNumber(String collectionNumber) {
        return bookStatusHistoryMapper.selectByCollectionNumber(collectionNumber);
    }
    
    /**
     * 查询最近的状态历史
     */
    public List<BookStatusHistory> getRecentStatusHistoryByCollectionNumber(String collectionNumber, int limit) {
        return bookStatusHistoryMapper.selectRecentByCollectionNumber(collectionNumber, limit);
    }
}