-- =========================================================================
-- TASK 2.1: KHỞI TẠO CƠ SỞ DỮ LIỆU SATELLITE SIMULATION
-- =========================================================================

-- 1. Tạo Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'SatelliteSimulation')
BEGIN
    CREATE DATABASE SatelliteSimulation;
END
GO

USE SatelliteSimulation;
GO

-- 2. Tạo Table SpaceObjects
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SpaceObjects]') AND type in (N'U'))
BEGIN
    CREATE TABLE SpaceObjects (
        ID NVARCHAR(50) NOT NULL PRIMARY KEY,
        Name NVARCHAR(100) NOT NULL,
        Type INT NOT NULL, -- 0: GroundStation, 1: Satellite
        Latitude FLOAT NOT NULL,
        Longitude FLOAT NOT NULL,
        Altitude FLOAT NOT NULL
    );
    PRINT 'Created table SpaceObjects';
END
ELSE
BEGIN
    PRINT 'Table SpaceObjects already exists';
END
GO

-- 3. Xóa dữ liệu cũ (nếu muốn reset)
DELETE FROM SpaceObjects;
GO

-- 4. Thêm Mock Data (Dữ liệu mẫu)
-- Dữ liệu này dùng để test giao diện và thuật toán hiển thị/định tuyến ban đầu

-- 4.1. Thêm các Trạm mặt đất (Type = 0, Altitude = 0)
INSERT INTO SpaceObjects (ID, Name, Type, Latitude, Longitude, Altitude) VALUES 
('hanoi-gs-001', 'Hanoi Station', 0, 21.0285, 105.8542, 0.0),
('tokyo-gs-002', 'Tokyo Station', 0, 35.6762, 139.6503, 0.0),
('ny-gs-003', 'New York Station', 0, 40.7128, -74.0060, 0.0),
('london-gs-004', 'London Station', 0, 51.5074, -0.1278, 0.0),
('sydney-gs-005', 'Sydney Station', 0, -33.8688, 151.2093, 0.0);
GO

-- 4.2. Thêm các Vệ tinh (Type = 1, Altitude > 0)
-- 10 Vệ tinh mẫu mô phỏng quỹ đạo LEO (Low Earth Orbit, độ cao khoảng 400-2000 km)
INSERT INTO SpaceObjects (ID, Name, Type, Latitude, Longitude, Altitude) VALUES 
('sat-001', 'LEO-Sat-1', 1, 0.0, 0.0, 400.0), -- Xích đạo
('sat-002', 'LEO-Sat-2', 1, 0.0, 45.0, 400.0),
('sat-003', 'LEO-Sat-3', 1, 0.0, 90.0, 400.0),
('sat-004', 'LEO-Sat-4', 1, 0.0, 135.0, 400.0),
('sat-005', 'LEO-Sat-5', 1, 0.0, -180.0, 400.0),

('sat-006', 'LEO-Sat-6', 1, 45.0, 0.0, 800.0), -- Vĩ độ trung bình Bắc
('sat-007', 'LEO-Sat-7', 1, 45.0, 90.0, 800.0),
('sat-008', 'LEO-Sat-8', 1, -45.0, 0.0, 800.0), -- Vĩ độ trung bình Nam
('sat-009', 'LEO-Sat-9', 1, -45.0, 90.0, 800.0),
('sat-010', 'GEO-Sat-1', 1, 0.0, 105.0, 35786.0); -- Vệ tinh địa tĩnh GEO, phủ sóng dải Châu Á
GO

-- 5. Kiểm tra lại dữ liệu vừa thêm
SELECT 'Ground Stations' AS ObjectGroup, COUNT(*) AS TotalCount FROM SpaceObjects WHERE Type = 0
UNION ALL
SELECT 'Satellites' AS ObjectGroup, COUNT(*) AS TotalCount FROM SpaceObjects WHERE Type = 1;
GO
