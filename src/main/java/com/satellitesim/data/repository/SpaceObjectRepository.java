package com.satellitesim.data.repository;

import com.satellitesim.core.models.SpaceObject;

import java.util.List;
import java.util.Optional;

/**
 * Interface Repository cho các thao tác CRUD trên SpaceObject.
 *
 * <p>Định nghĩa contract cho lớp truy xuất dữ liệu (Data Access Layer).
 * Implementation cụ thể sẽ sử dụng JDBC để giao tiếp với SQL Server.</p>
 *
 * <p>Tuân thủ nguyên tắc Dependency Inversion (SOLID):
 * Service layer phụ thuộc vào interface này, không phụ thuộc vào implementation.</p>
 */
public interface SpaceObjectRepository {

    /**
     * Lấy tất cả SpaceObject từ CSDL.
     *
     * @return Danh sách tất cả vật thể
     */
    List<SpaceObject> findAll();

    /**
     * Tìm SpaceObject theo ID.
     *
     * @param id Định danh duy nhất
     * @return Optional chứa vật thể nếu tìm thấy
     */
    Optional<SpaceObject> findById(String id);

    /**
     * Lưu (insert hoặc update) một SpaceObject.
     *
     * @param spaceObject Vật thể cần lưu
     * @return true nếu thao tác thành công
     */
    boolean save(SpaceObject spaceObject);

    /**
     * Xóa SpaceObject theo ID.
     *
     * @param id Định danh duy nhất
     * @return true nếu xóa thành công
     */
    boolean deleteById(String id);

    /**
     * Lưu nhiều SpaceObject cùng lúc (batch insert).
     *
     * @param spaceObjects Danh sách vật thể cần lưu
     * @return Số lượng bản ghi đã lưu thành công
     */
    int saveAll(List<SpaceObject> spaceObjects);
}
