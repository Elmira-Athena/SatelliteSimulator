package com.satellitesim.services;

import com.satellitesim.core.models.SpaceObject;
import com.satellitesim.data.repository.SpaceObjectRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service layer quản lý các SpaceObject (Business Logic Layer).
 *
 * <p>Đây là lớp trung gian giữa UI và Data layer, chứa logic nghiệp vụ
 * liên quan đến quản lý vật thể không gian. Tuân thủ nguyên tắc
 * Single Responsibility: chỉ xử lý CRUD nghiệp vụ, không chứa logic
 * vật lý hay rendering.</p>
 */
public class SpaceObjectService {

    private final SpaceObjectRepository repository;

    /**
     * Constructor injection - tuân thủ Dependency Inversion Principle.
     *
     * @param repository Repository implementation
     */
    public SpaceObjectService(SpaceObjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Lấy tất cả vật thể không gian.
     *
     * @return Danh sách SpaceObject
     */
    public List<SpaceObject> getAllSpaceObjects() {
        return repository.findAll();
    }

    /**
     * Tìm vật thể theo ID.
     *
     * @param id Định danh
     * @return Optional chứa vật thể nếu tìm thấy
     */
    public Optional<SpaceObject> getSpaceObjectById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) một vật thể.
     *
     * @param spaceObject Vật thể cần lưu
     * @return true nếu thành công
     * @throws IllegalArgumentException nếu đối tượng null
     */
    public boolean saveSpaceObject(SpaceObject spaceObject) {
        if (spaceObject == null) {
            throw new IllegalArgumentException("SpaceObject cannot be null");
        }
        return repository.save(spaceObject);
    }

    /**
     * Xóa vật thể theo ID.
     *
     * @param id Định danh
     * @return true nếu xóa thành công
     */
    public boolean deleteSpaceObject(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        return repository.deleteById(id);
    }

    /**
     * Lưu nhiều vật thể cùng lúc (batch).
     *
     * @param spaceObjects Danh sách vật thể cần lưu
     * @return Số lượng bản ghi đã lưu thành công
     */
    public int saveAllSpaceObjects(List<SpaceObject> spaceObjects) {
        if (spaceObjects == null || spaceObjects.isEmpty()) {
            return 0;
        }
        return repository.saveAll(spaceObjects);
    }
}
