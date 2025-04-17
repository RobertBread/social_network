package com.example.guiex1.repository.dbrepo;

import com.example.guiex1.domain.Entity;
import com.example.guiex1.repository.Repository;
import com.example.guiex1.utils.paging.Pageable;
import com.example.guiex1.utils.paging.Page;

public interface PagingRepository<ID , E extends Entity<ID>> extends Repository<ID, E> {
    Page<E> findAllOnPage(Pageable pageable);
}