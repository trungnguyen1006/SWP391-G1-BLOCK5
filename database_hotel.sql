
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `booking_hotel_db`
--
DROP DATABASE IF EXISTS hotelManage;
CREATE DATABASE hotelManage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotelManage;

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` bigint NOT NULL,
  `avatar_url` varchar(1000) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role_id` enum('ADMIN','CUSTOMER','GUEST','RECEPTIONIST') NOT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL,
  `phone` VARCHAR(20) NOT NULL UNIQUE,
  `address` VARCHAR(255),
  `username` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `uk_users_username` (`username`),
  ADD UNIQUE KEY `uk_users_email` (`email`);
  
  --
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

ALTER TABLE users
    MODIFY role_id ENUM('ADMIN', 'CUSTOMER', 'MANAGER', 'RECEPTIONIST');

--
-- Table structure for table `room_type`
--

CREATE TABLE `room_type` (
  `room_type_id`   bigint AUTO_INCREMENT PRIMARY KEY,
  `room_type_name` VARCHAR(255) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `description`   TEXT,
  `device`   TEXT,
  `utilities` TEXT,
  `price`         DECIMAL(10,2) NOT NULL,
  `amount_person` INT NOT NULL
) ENGINE=InnoDB;

--
-- Table structure for table `room_image`
--
CREATE TABLE `room_type_image` (
  `image_id`      bigint AUTO_INCREMENT PRIMARY KEY,
  `image_url`     VARCHAR(500) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_primary`    BOOLEAN DEFAULT FALSE,
  `room_type_id`   bigint NOT NULL,
  CONSTRAINT `fk_image_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `room_type`(`room_type_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

--
-- Table structure for table `room`
--

CREATE TABLE `room` (
  `room_id`       bigint AUTO_INCREMENT PRIMARY KEY,
  `room_number`   VARCHAR(25) NOT NULL,
  `status` enum('AVAILABLE','OCCUPIED','UNDER_MAINTENANCE') NOT NULL,
  `room_type_id`   bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  CONSTRAINT `fk_room_type` FOREIGN KEY (`room_type_id`) REFERENCES `Room_Type`(`room_type_id`)
) ENGINE=InnoDB;

CREATE UNIQUE INDEX `uq_room_number` ON `Room`(`room_number`);
CREATE INDEX `idx_room_roomtype` ON `Room`(`room_type_id`);

-- --------------------------------------------------------

INSERT INTO `room_type` (`room_type_name`, `description`, `device`, `utilities`, `price`, `amount_person`)
VALUES
-- Phòng tiêu chuẩn
('Standard Room', 
 'Phòng tiêu chuẩn phù hợp cho 1-2 người, thiết kế đơn giản nhưng đầy đủ tiện nghi.', 
 'Giường đôi, TV, máy lạnh, bàn làm việc', 
 'Wifi miễn phí, nước suối, dọn phòng hàng ngày', 
 500000, 
 2),

-- Phòng Deluxe
('Deluxe Room', 
 'Phòng Deluxe rộng rãi, có cửa sổ lớn hướng phố hoặc hồ.', 
 'Giường Queen, TV 42 inch, tủ lạnh mini, bàn trang điểm', 
 'Wifi miễn phí, trà/cà phê miễn phí, dọn phòng hàng ngày, bữa sáng kèm theo', 
 800000, 
 2),

-- Phòng Suite
('Suite Room', 
 'Phòng Suite cao cấp với phòng khách riêng, view thành phố.', 
 'Giường King, sofa, TV 55 inch, bồn tắm, két sắt', 
 'Wifi tốc độ cao, phục vụ ăn sáng tại phòng, minibar, dịch vụ giặt ủi', 
 1500000, 
 3),

-- Phòng Family
('Family Room', 
 'Phòng Family dành cho gia đình hoặc nhóm bạn, có 2 giường lớn.', 
 '2 Giường Queen, TV, tủ lạnh, bàn ăn nhỏ', 
 'Wifi miễn phí, bữa sáng, nước suối, dịch vụ phòng', 
 1200000, 
 4),

-- Phòng VIP
('VIP Room', 
 'Phòng VIP sang trọng, nằm ở tầng cao với view toàn cảnh, nội thất cao cấp.', 
 'Giường King, TV OLED 65 inch, máy pha cà phê, phòng tắm hơi riêng', 
 'Wifi tốc độ cao, xe đưa đón sân bay, phục vụ riêng, ăn sáng buffet', 
 2500000, 
 2);

INSERT INTO `room` (`room_number`, `status`, `room_type_id`)
VALUES
-- Standard Room (roomType_id = 1)
('101', 'AVAILABLE', 1),
('102', 'AVAILABLE', 1),
('103', 'AVAILABLE', 1),
('104', 'AVAILABLE', 1),

-- Deluxe Room (roomType_id = 2)
('201', 'AVAILABLE', 2),
('202', 'AVAILABLE', 2),
('203', 'AVAILABLE', 2),
('204', 'AVAILABLE', 2),

-- Suite Room (roomType_id = 3)
('301', 'AVAILABLE', 3),
('302', 'AVAILABLE', 3),
('303', 'AVAILABLE', 3),

-- Family Room (roomType_id = 4)
('401', 'AVAILABLE', 4),
('402', 'AVAILABLE', 4),
('403', 'AVAILABLE', 4),

-- VIP Room (roomType_id = 5)
('501', 'AVAILABLE', 5),
('502', 'AVAILABLE', 5),
('503', 'AVAILABLE', 5);

INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (1,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514624/hotel/room-types/qsmndlmjdtvug6h9ap9m.webp',NULL,1,1);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (2,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514643/hotel/room-types/b8nohj7jh8kvpqblbd2z.webp',NULL,0,1);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (3,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514645/hotel/room-types/ptq6lg8ahdsonujqfkbm.webp',NULL,0,1);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (4,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514646/hotel/room-types/ugxcljpiajn8oxogpppe.webp',NULL,0,1);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (5,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514648/hotel/room-types/b7khsvszhbzzvkuuqamu.webp',NULL,0,1);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (6,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514676/hotel/room-types/c6nivfd3ngtjpiyrijzx.webp',NULL,1,2);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (7,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514688/hotel/room-types/drtdqzz9lg3y9srvxf8c.webp',NULL,0,2);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (8,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514690/hotel/room-types/fbbwnnnbauugs8yv6djn.webp',NULL,0,2);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (9,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514692/hotel/room-types/poevk32jsrb3zxqnlkjl.webp',NULL,0,2);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (10,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514694/hotel/room-types/oxhf0myfm3mpbgqoxzgp.webp',NULL,0,2);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (11,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514728/hotel/room-types/h1eviiiuamfsugo5ruwm.webp',NULL,1,3);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (12,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514753/hotel/room-types/aepcahghr0ddyl5wtq9z.webp',NULL,0,3);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (13,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514755/hotel/room-types/f4roerzjbq3vlb8hizy2.webp',NULL,0,3);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (14,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514756/hotel/room-types/iexybg8yfqugutjuruj7.webp',NULL,0,3);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (15,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514758/hotel/room-types/hxu6letgqem5jszdbvzr.webp',NULL,0,3);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (16,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514775/hotel/room-types/ywkr99dcirivvqcf7bk4.jpg',NULL,1,4);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (17,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514787/hotel/room-types/yqoqjdiwcmpwic6veqfc.webp',NULL,0,4);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (18,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514789/hotel/room-types/gurqmkonokbau8sqhqbv.webp',NULL,0,4);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (19,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514791/hotel/room-types/izaz27lpphtsvbvyqoaa.webp',NULL,0,4);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (20,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514793/hotel/room-types/u0wrdod19plq6dmtskrf.webp',NULL,0,4);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (21,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514806/hotel/room-types/sdxiw75ydlqvq6xteh0b.webp',NULL,1,5);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (22,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514817/hotel/room-types/eeiqibhq93fez2sudzkg.webp',NULL,0,5);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (23,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514818/hotel/room-types/avkupnlcgkkx8ysvcb08.webp',NULL,0,5);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (24,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514820/hotel/room-types/m9rlxaw6spuff0urcaz8.webp',NULL,0,5);
INSERT INTO `room_type_image` (`image_id`,`image_url`,`deleted_at`,`is_primary`,`room_type_id`) VALUES (25,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762514822/hotel/room-types/hanvefh5mbidmmdkehxp.webp',NULL,0,5);

-- --------------------------------------------------------

--
-- Table structure for table `amenity`
--

CREATE TABLE `amenity` (
  `amenity_id` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  `amenity_name` varchar(255) NOT NULL,
  `description` LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `blog`
--

CREATE TABLE `blog` (
  `blog_id` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `excerpt` varchar(500) DEFAULT NULL,
  `content` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `blog`
--

INSERT INTO `blog` (`blog_id`, `deleted_at`, `image_url`, `title`, `excerpt`, `content`) VALUES
(1, NULL, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?q=80&w=1200&auto=format&fit=crop', '10 mẹo đặt phòng khách sạn tiết kiệm', '10 mẹo giúp bạn tiết kiệm đáng kể chi phí lưu trú, biến kỳ nghỉ trong mơ thành hiện thực.', '<h1>10 mẹo đặt phòng khách sạn tiết kiệm & thông minh</h1>\r\n<p>Chi phí lưu trú thường chiếm một phần lớn ngân sách du lịch. Để có một chuyến đi trọn vẹn mà vẫn tối ưu hầu bao, việc nắm vững các \"bí kíp\" đặt phòng là điều vô cùng cần thiết. Dưới đây là 10 mẹo thực tế giúp bạn đặt phòng vừa <b>tiết kiệm</b> vừa <b>an tâm</b>.</p>\r\n\r\n<hr/>\r\n<h2>1. Theo dõi giá & Chọn đúng thời điểm</h2>\r\n<p>Giá phòng khách sạn là một thị trường biến động. Nguyên tắc cơ bản là <strong>đặt càng sớm càng tốt</strong>, đặc biệt là vào mùa cao điểm (Hè, Lễ Tết). Giá thường thấp hơn vào các ngày trong tuần (Chủ Nhật đến Thứ Năm) và tăng mạnh vào cuối tuần.</p>\r\n<blockquote><b>Mẹo nhỏ:</b> Dùng chức năng <i>Cảnh báo giá</i> (Price Alert) trên các ứng dụng du lịch (OTA) để nhận thông báo qua email hoặc app khi giá giảm.</blockquote>\r\n\r\n<h2>2. Tận dụng tối đa Mã ưu đãi & Chương trình thành viên (Loyalty)</h2>\r\n<p>Hầu hết các chuỗi khách sạn lớn và nền tảng đặt phòng (Booking, Agoda, Traveloka…) đều có chương trình tích điểm. Đừng bỏ qua:</p>\r\n<ul>\r\n  <li><b>Điểm thưởng:</b> Tích lũy để đổi lấy đêm nghỉ miễn phí hoặc giảm giá.</li>\r\n  <li><b>Ưu đãi thẻ ngân hàng:</b> Nhiều ngân hàng hợp tác với OTA để cung cấp mã giảm giá độc quyền.</li>\r\n  <li><b>Đăng ký Newsletter:</b> Khách sạn hoặc OTA thường gửi mã khuyến mãi qua email cho người đăng ký.</li>\r\n</ul>\r\n\r\n<h2>3. So sánh giá trên nhiều Kênh đặt phòng (OTA)</h2>\r\n<p>Đừng bao giờ chỉ kiểm tra một website. Mỗi OTA có những hợp đồng và chương trình khuyến mãi riêng biệt. Hãy dùng các công cụ so sánh giá (như Google Hotels) để có cái nhìn tổng quan nhất.</p>\r\n\r\n<h2>4. Linh hoạt về Ngày ở và Loại phòng</h2>\r\n<ol>\r\n  <li><b>Thay đổi ngày check-in/check-out:</b> Đôi khi chỉ cần dịch chuyển 1 ngày đã giúp bạn tiết kiệm đáng kể.</li>\r\n  <li><b>Lựa chọn loại phòng:</b> Phòng có view không quá đẹp, tầng thấp hoặc phòng đơn có thể rẻ hơn. Bạn luôn có thể yêu cầu nâng cấp (up-sell) khi đến nơi nếu khách sạn còn phòng trống.</li>\r\n</ol>\r\n\r\n<h2>5. Liên hệ Trực tiếp Khách sạn (Book Direct)</h2>\r\n<p>Sau khi đã có mức giá tham khảo tốt nhất từ OTA, hãy gọi điện hoặc gửi email trực tiếp cho khách sạn. Đôi khi họ sẽ đưa ra mức giá ngang bằng hoặc tốt hơn, kèm theo những đặc quyền như: <em>late check-out</em>, bữa sáng miễn phí, hoặc nâng cấp phòng, vì họ không phải trả hoa hồng cho bên thứ ba.</p>\r\n\r\n<h2>6. Cân nhắc Combo Vé máy bay + Khách sạn</h2>\r\n<p>Nhiều hãng hàng không và OTA cung cấp gói combo này với giá rẻ hơn đáng kể so với việc đặt riêng lẻ. Đây là lựa chọn tối ưu nếu bạn đã xác định chắc chắn cả lịch bay và nơi ở.</p>\r\n\r\n<h2>7. Chính sách \"Ở lâu hơn để có giá tốt\" (Long-stay Deal)</h2>\r\n<p>Nếu bạn có kế hoạch ở từ 3 đêm trở lên, hãy tìm các gói khuyến mãi dành cho lưu trú dài ngày (Long-stay Package). Khách sạn luôn ưu tiên khách ở lâu để tối ưu hóa công suất phòng.</p>\r\n\r\n<h2>8. Đọc kỹ Chính sách hủy & Phụ thu</h2>\r\n<p>Phòng không hoàn hủy (Non-refundable) luôn rẻ hơn. Tuy nhiên, nếu lịch trình có thể thay đổi, hãy chọn loại phòng có thể hủy miễn phí. Luôn kiểm tra kỹ:</p>\r\n<ul>\r\n  <li>Phí VAT, phí dịch vụ (đã bao gồm hay chưa).</li>\r\n  <li>Phụ thu người thứ 3, trẻ em, và bữa sáng.</li>\r\n</ul>\r\n\r\n<h2>9. Chọn vị trí phù hợp, không nhất thiết là trung tâm tuyệt đối</h2>\r\n<p>Các khách sạn ở khu vực lân cận, cách trung tâm một quãng di chuyển ngắn bằng phương tiện công cộng, thường rẻ hơn nhiều. Cân nhắc chi phí đi lại và thời gian để quyết định, đôi khi sự yên tĩnh lại là một giá trị cộng thêm.</p>\r\n\r\n<h2>10. Đặt sớm nhưng vẫn Canh giá lại</h2>\r\n<p>Nếu bạn đã đặt phòng với chính sách <b>hủy miễn phí</b>, hãy tiếp tục theo dõi giá. Nếu giá giảm mạnh gần ngày đi, bạn có thể hủy booking cũ và đặt lại với giá tốt hơn mà không mất phí.</p>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Tiết kiệm chi phí lưu trú là một nghệ thuật. Không có một bí kíp nào cố định cho mọi chuyến đi. Quan trọng là sự <i>linh hoạt</i>, kết hợp các mẹo trên, theo dõi giá và luôn đặt phòng sớm. Chúc bạn luôn tìm được mức giá ưng ý và có một kỳ nghỉ tuyệt vời!</p>'),
(2, NULL, 'https://images.unsplash.com/photo-1470337458703-46ad1756a187?q=80&w=1200&auto=format&fit=crop', 'Trải nghiệm ẩm thực địa phương đáng thử', 'Hành trình khám phá ẩm thực địa phương đậm đà bản sắc: từ vỉa hè đến nhà hàng truyền thống.', '<h1>Trải nghiệm ẩm thực địa phương đáng thử: Chiếc chìa khóa văn hóa</h1>\n<p>Đối với một người mê xê dịch, ẩm thực không chỉ là để no bụng mà còn là chiếc chìa khóa mở cánh cửa văn hoá của một vùng đất. Từ quán vỉa hè giản dị đến nhà hàng lâu đời, mỗi món ăn đều mang trong mình một câu chuyện về lịch sử, con người và thiên nhiên nơi nó sinh ra.</p>\n\n<hr/>\n<h2>1. Câu chuyện trong hương vị bản địa</h2>\n<p>Mỗi địa phương có một \"chữ ký\" ẩm thực riêng, được tạo nên từ:</p>\n<ul>\n  <li><b>Nguyên liệu bản địa:</b> Cá tươi từ biển, rau rừng, gạo đặc sản tạo nên hương vị không thể trộn lẫn.</li>\n  <li><b>Gia vị truyền thống:</b> Nước mắm, mắm tôm, các loại lá thơm được sử dụng theo công thức đời cha ông.</li>\n  <li><b>Kỹ thuật chế biến:</b> Cách nướng than, cách ủ mắm, cách hầm xương... thể hiện sự tinh tế của người đầu bếp địa phương.</li>\n</ul>\n\n<h2>2. Gợi ý hành trình khám phá ẩm thực đích thực</h2>\n<p>Làm thế nào để thoát khỏi những \"bẫy du lịch\" và tìm đến hương vị chân thật nhất?</p>\n<ol>\n  <li>\n    <h3>Tham gia Food Tour cùng người bản địa</h3>\n    <p>Đây là cách nhanh nhất để thử nhiều món, nghe kể về lịch sử và văn hóa ẩm thực. Hướng dẫn viên địa phương biết chính xác quán \"ruột\" và thời điểm ngon nhất.</p>\n  </li>\n  <li>\n    <h3>Ghé thăm Chợ truyền thống vào buổi sáng</h3>\n    <p>Chợ là nơi bạn cảm nhận nhịp sống và tìm thấy những món ăn sáng, quà vặt mà người dân địa phương dùng hàng ngày. Đừng ngại thử các món ăn vặt được bán trong chợ.</p>\n  </li>\n  <li>\n    <h3>Áp dụng \"Bí kíp Hỏi Địa phương\"</h3>\n    <p>Hỏi tài xế taxi, nhân viên khách sạn hoặc người bán hàng về quán ăn mà họ thường xuyên ghé. Đây thường là những nơi có chất lượng ổn định và giá cả phải chăng.</p>\n  </li>\n</ol>\n\n<blockquote>\"Du lịch là trải nghiệm. Ẩm thực là trải nghiệm sâu sắc nhất, giúp bạn kết nối trực tiếp với linh hồn của một vùng đất.\"</blockquote>\n\n<h2>3. Vượt qua rào cản Ngôn ngữ & Vệ sinh</h2>\n<ul>\n  <li><b>Ngôn ngữ:</b> Chuẩn bị sẵn hình ảnh món ăn nếu bạn đến những nơi không có menu song ngữ.</li>\n  <li><b>Vệ sinh:</b> Chọn những quán đông khách, có lượt phục vụ liên tục – đây thường là dấu hiệu của nguyên liệu tươi mới.</li>\n</ul>\n\n<hr/>\n<h2>Kết luận</h2>\n<p>Ăn ngon không chỉ là hương vị, mà là cảm giác được kết nối, là sự tôn trọng với văn hóa. Hãy mạnh dạn bước ra khỏi khu vực an toàn và để vị giác dẫn lối cho chuyến phiêu lưu của bạn.</p>'),
(3, NULL, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1200&auto=format&fit=crop', 'Checklist chuẩn bị cho chuyến du lịch biển', 'Checklist tối giản và thiết yếu cho chuyến du lịch biển hoàn hảo, từ trang phục đến vật dụng y tế cơ bản.', '<h1>Checklist chuẩn bị cho chuyến du lịch biển từ A đến Z</h1>\r\n<p>Biển gọi, chúng ta đi thôi! Nhưng để kỳ nghỉ thảnh thơi và trọn vẹn, việc chuẩn bị đồ đạc kỹ lưỡng là bước không thể thiếu. Một checklist thông minh giúp bạn mang đủ những món đồ nhỏ mà “có cũng như không, thiếu lại nhớ cả chuyến”.</p>\r\n\r\n<hr/>\r\n<h2>1. Vật dụng bảo vệ da & Sức khỏe</h2>\r\n<p>Đây là nhóm quan trọng nhất khi đi biển, nơi tia UV mạnh hơn đáng kể.</p>\r\n<ul>\r\n  <li><b>Kem chống nắng phổ rộng (SPF 50+):</b> Dùng cho cả mặt và cơ thể, thoa lại sau mỗi 2-3 giờ hoặc sau khi bơi.</li>\r\n  <li><b>Dưỡng da sau nắng (After Sun):</b> Giúp làm dịu và phục hồi da bị cháy nắng.</li>\r\n  <li><b>Kính râm, nón rộng vành:</b> Bảo vệ mắt và da đầu khỏi nắng gắt.</li>\r\n  <li><b>Thuốc y tế cơ bản:</b> Thuốc say sóng, thuốc hạ sốt, đau đầu, băng cá nhân, và các loại thuốc đặc trị cá nhân.</li>\r\n</ul>\r\n\r\n<h2>2. Trang phục & Phụ kiện bơi lội</h2>\r\n<p>Ưu tiên chất liệu nhẹ, mau khô và dễ gấp gọn.</p>\r\n<ol>\r\n  <li><b>Đồ bơi/Bikini:</b> Nên mang theo 2-3 bộ để thay đổi và đảm bảo luôn có đồ khô ráo.</li>\r\n  <li><b>Khăn tắm/Khăn choàng nhẹ (Sarong):</b> Dùng để che chắn, nằm phơi nắng hoặc khoác nhẹ khi đi dạo.</li>\r\n  <li><b>Túi chống nước chuyên dụng:</b> Để điện thoại, tiền mặt khi tắm biển hoặc đi thuyền.</li>\r\n  <li><b>Dép đi biển (Flip-flops) & Túi Tote:</b> Dép chịu nước và túi lớn để đựng đồ ướt.</li>\r\n</ol>\r\n\r\n<blockquote><b>Mẹo đóng gói:</b> Cuộn tròn quần áo thay vì gấp để tiết kiệm không gian vali và tránh làm nhăn.</blockquote>\r\n\r\n<h2>3. Thiết bị Điện tử & Giải trí</h2>\r\n<ul>\r\n  <li><b>Sạc dự phòng, Adapter:</b> Đảm bảo thiết bị luôn sẵn sàng chụp ảnh.</li>\r\n  <li><b>Máy ảnh/GoPro:</b> Để ghi lại những khoảnh khắc dưới nước hoặc các hoạt động mạo hiểm.</li>\r\n  <li><b>Sách/Máy đọc sách:</b> Để thư giãn dưới bóng mát cây dừa.</li>\r\n</ul>\r\n\r\n<h2>4. Những thứ dễ quên nhưng cần thiết</h2>\r\n<p>Đừng quên những vật dụng nhỏ nhưng có tác động lớn đến sự thoải mái của bạn.</p>\r\n<ul>\r\n  <li>Thuốc chống côn trùng.</li>\r\n  <li>Dầu gội, sữa tắm cỡ du lịch.</li>\r\n  <li>Túi ni-lông hoặc túi giặt (Laundry Bag) để đựng đồ bẩn/đồ ướt.</li>\r\n</ul>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Một chiếc vali gọn gàng, đủ đồ sẽ đảm bảo bạn chỉ cần tập trung vào việc tận hưởng nắng vàng, biển xanh và cát trắng. Hãy chuẩn bị kỹ lưỡng để tận hưởng kỳ nghỉ thảnh thơi nhất!</p>'),
(4, NULL, 'https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?q=80&w=1200&auto=format&fit=crop', 'Bí kíp săn vé máy bay giá tốt', 'Bí kíp canh vé rẻ: thời điểm vàng đặt vé, cách tối ưu lịch trình bay và sử dụng công cụ thông minh.', '<h1>Bí kíp săn vé máy bay giá tốt: Cuộc chơi của người thông minh</h1>\r\n<p>Vé máy bay giá rẻ không phải là may mắn. Đó là “cuộc chơi” của dữ liệu, sự kiên nhẫn và thói quen đặt vé thông minh. Nắm vững những bí kíp sau, bạn sẽ luôn tìm được mức giá ưng ý mà không cần chờ đợi những đợt sale \"sốc\" hiếm hoi.</p>\r\n\r\n<hr/>\r\n<h2>1. Xác định \"Thời điểm vàng\" để đặt vé</h2>\r\n<p>Các nghiên cứu chỉ ra rằng có những khung thời gian cụ thể giúp bạn dễ dàng có được giá tốt nhất:</p>\r\n<ul>\r\n  <li><b>Thời điểm lý tưởng:</b> Đặt trước 6 - 8 tuần cho chuyến bay nội địa và 2-4 tháng cho chuyến bay quốc tế.</li>\r\n  <li><b>Ngày đặt vé tốt nhất:</b> Thường là Thứ Ba hoặc Thứ Tư, vì các hãng hàng không thường cập nhật giá và tung khuyến mãi vào đầu tuần.</li>\r\n  <li><b>Tránh đặt vào cuối tuần:</b> Giá có xu hướng tăng cao vào Thứ Sáu, Thứ Bảy, Chủ Nhật.</li>\r\n</ul>\r\n\r\n<h2>2. Tối ưu hóa Lịch trình bay</h2>\r\n<p>Sự linh hoạt về ngày và giờ bay có thể giúp bạn tiết kiệm hàng triệu đồng.</p>\r\n<ol>\r\n  <li><b>Chọn bay Red-eye hoặc giữa tuần:</b> Các chuyến bay khởi hành rất sớm, rất muộn hoặc vào giữa tuần (Thứ Ba, Thứ Tư) thường có nhu cầu thấp hơn, kéo theo giá vé rẻ hơn.</li>\r\n  <li><b>Bay đến sân bay phụ:</b> Nếu điểm đến có nhiều sân bay (ví dụ: New York), hãy kiểm tra giá đến sân bay ít phổ biến hơn.</li>\r\n  <li><b>Nối chuyến thay vì Bay thẳng:</b> Đối với các chuyến quốc tế dài, việc chấp nhận 1 điểm dừng (layover) đôi khi giúp giảm giá đáng kể.</li>\r\n</ol>\r\n\r\n<blockquote><b>Mẹo tối ưu:</b> Xóa cookies/cache hoặc dùng chế độ Ẩn danh (Incognito Mode) khi tìm kiếm để tránh bị theo dõi và đẩy giá lên cao.</blockquote>\r\n\r\n<h2>3. Tận dụng công nghệ & Ưu đãi độc quyền</h2>\r\n<ul>\r\n  <li><b>Sử dụng công cụ so sánh (Kayak, Skyscanner, Google Flights):</b> Dùng tính năng \"Tìm kiếm linh hoạt\" hoặc \"Cả tháng\" để xem giá thấp nhất trong cả chu kỳ.</li>\r\n  <li><b>Đặt qua App/Ứng dụng di động:</b> Các hãng hàng không và đại lý thường tung mã giảm giá độc quyền hoặc ưu đãi flash sale chỉ dành cho người dùng app.</li>\r\n  <li><b>Theo dõi các trang Fanpage/Group săn vé:</b> Tham gia cộng đồng để nhận thông báo khuyến mãi nhanh nhất.</li>\r\n</ul>\r\n\r\n<h2>4. Lựa chọn hãng hàng không thông minh</h2>\r\n<p>Cân nhắc các hãng hàng không giá rẻ (Low-cost carriers) nếu bạn không có nhiều hành lý và không quá khắt khe về dịch vụ đi kèm. Luôn tính toán tổng chi phí (vé + hành lý ký gửi + chọn chỗ) để so sánh công bằng với vé của hãng truyền thống.</p>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Săn vé rẻ là một thói quen cần rèn luyện. Bắt đầu bằng việc đặt sớm, linh hoạt lịch trình và sử dụng công cụ thông minh. Chúc bạn luôn tìm được mức giá ưng ý và sẵn sàng cho những hành trình khám phá!</p>'),
(5, NULL, 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1200&auto=format&fit=crop', 'Top 5 điểm đến lãng mạn mùa thu', '5 gợi ý lãng mạn giữa tiết trời thu dịu nhẹ: nơi có lá vàng, không khí se lạnh và sự yên bình tuyệt đối.', '<h1>Top 5 điểm đến lãng mạn nhất mùa thu Việt Nam</h1>\r\n<p>Mùa thu là thời khắc của những gam màu ấm áp, bầu không khí yên bình và sự lãng mạn dịu dàng. Nếu bạn đang tìm kiếm một nơi để \"trốn\" khỏi cái nóng hè và tận hưởng tiết trời se lạnh, 5 điểm đến sau đây sẽ là lựa chọn hoàn hảo cho một kỳ nghỉ đôi lứa hoặc chuyến du lịch chữa lành.</p>\r\n\r\n<hr/>\r\n<h2>1. Đà Lạt – Thành phố ngàn hoa chìm trong sương</h2>\r\n<p>Mùa thu, Đà Lạt đón du khách bằng những đồi thông mờ sương và không khí lạnh sâu. Nơi đây lãng mạn tuyệt đối với:</p>\r\n<ul>\r\n  <li><b>Sắc màu:</b> Cỏ hồng (Pink Grass), hoa Dã Quỳ vàng rực rỡ bắt đầu nở rộ.</li>\r\n  <li><b>Hoạt động:</b> Thưởng thức cà phê nóng, chèo thuyền trên Hồ Tuyền Lâm, hoặc thuê xe máy dạo quanh các cung đường vắng.</li>\r\n</ul>\r\n\r\n<h2>2. Sapa – Mùa lúa vàng trên ruộng bậc thang</h2>\r\n<p>Thu đến là lúc Sapa khoác lên mình màu vàng óng ả của những thửa ruộng bậc thang. Đây là thời điểm đẹp nhất để:</p>\r\n<ul>\r\n  <li><b>Trải nghiệm:</b> Trekking qua các bản làng Tả Van, Lao Chải ngắm lúa chín.</li>\r\n  <li><b>Không khí:</b> Cảm nhận cái lạnh vùng cao, thưởng thức thắng cố và các món nướng bên bếp lửa hồng.</li>\r\n</ul>\r\n\r\n<h2>3. Hà Nội – Nét đẹp cổ kính của mùa lá bay</h2>\r\n<p>Mùa thu Hà Nội là nguồn cảm hứng bất tận của thi ca. Không khí dịu mát, thoảng hương hoa sữa là điểm nhấn lãng mạn không thể chối từ.</p>\r\n<ul>\r\n  <li><b>Món ăn mùa thu:</b> Cốm làng Vòng, sấu chín, và những gánh hàng hoa thu.</li>\r\n  <li><b>Góc phố lãng mạn:</b> Hồ Gươm, con đường Phan Đình Phùng với những hàng cây lá vàng rơi.</li>\r\n</ul>\r\n\r\n<blockquote>Mùa thu là mùa của sự kết nối, nơi chúng ta chậm lại và tận hưởng từng khoảnh khắc.</blockquote>\r\n\r\n<h2>4. Hội An – Nắng vàng ruộm trên phố cổ</h2>\r\n<p>Thu ở Hội An mang vẻ đẹp hoài cổ, tĩnh lặng. Nắng không còn gắt như hè, thay vào đó là ánh vàng ruộm phủ lên những mái nhà rêu phong.</p>\r\n<ul>\r\n  <li><b>Điểm nhấn:</b> Thả đèn hoa đăng trên sông Hoài vào đêm Rằm.</li>\r\n  <li><b>Hoạt động:</b> Thuê xe đạp dạo quanh phố cổ và ghé quán cà phê ven sông.</li>\r\n</ul>\r\n\r\n<h2>5. Ninh Bình – Vẻ đẹp hùng vĩ giữa tiết trời dịu nhẹ</h2>\r\n<p>Cố đô Hoa Lư vào mùa thu có không khí trong lành, mát mẻ, hoàn hảo cho các hoạt động khám phá thiên nhiên.</p>\r\n<ul>\r\n  <li><b>Khám phá:</b> Chèo thuyền Tam Cốc Bích Động, ngắm nhìn cánh đồng lúa chín vàng dưới chân núi.</li>\r\n  <li><b>Cảnh quan:</b> Quần thể danh thắng Tràng An, Hang Múa huyền ảo.</li>\r\n</ul>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Mỗi điểm đến mùa thu Việt Nam mang một phong vị rất riêng. Hãy chọn một nơi, lên kế hoạch cho một chuyến đi chậm rãi và tận hưởng trọn vẹn sự lãng mạn mà thiên nhiên ban tặng.</p>'),
(6, NULL, 'https://images.unsplash.com/photo-1504754524776-8f4f37790ca0?q=80&w=1200&auto=format&fit=crop', 'Hành trình khám phá ẩm thực đường phố', 'Khám phá bản đồ hương vị trên từng góc phố: những món ăn vỉa hè làm nên linh hồn của thành phố.', '<h1>Hành trình khám phá ẩm thực đường phố: Nơi linh hồn thành phố cư ngụ</h1>\r\n<p>Ẩm thực đường phố (Street Food) chính là “nhà hàng mở” lớn nhất, chân thật nhất của một thành phố. Ở đó, có mùi khói bếp than, tiếng rao hàng thân quen, và nụ cười rạng rỡ của người bán hàng. Đây là nơi bạn tìm thấy linh hồn và nhịp sống của người dân địa phương.</p>\r\n\r\n<hr/>\r\n<h2>1. Tại sao ẩm thực đường phố lại cuốn hút?</h2>\r\n<p>Street Food hấp dẫn không chỉ vì giá cả phải chăng mà còn vì những trải nghiệm độc đáo:</p>\r\n<ul>\r\n  <li><b>Tính chân thực:</b> Món ăn được chế biến ngay trước mắt bạn, không có sự cầu kỳ, kiểu cách.</li>\r\n  <li><b>Không khí:</b> Cảm giác ngồi quây quần bên vỉa hè, giao lưu với người dân địa phương.</li>\r\n  <li><b>Hương vị:</b> Thường là các công thức gia truyền, đậm đà, được điều chỉnh để hợp khẩu vị số đông.</li>\r\n</ul>\r\n\r\n<h2>2. Các món ăn \"Phải thử\" trên đường phố Việt Nam</h2>\r\n<p>Mỗi vùng miền đều có những món ăn vỉa hè kinh điển làm nên tên tuổi:</p>\r\n<ol>\r\n  <li><b>Bánh mì, Phở, Bún:</b> Bộ ba \"quốc hồn quốc túy\". Thử Bánh Mì Việt Nam – món ăn được thế giới công nhận là đỉnh cao của sự kết hợp Đông-Tây.</li>\r\n  <li><b>Hải sản & Đồ nướng vỉa hè:</b> Tuyệt vời cho buổi tối. Tôm, ốc, mực được nướng/xào tại chỗ, ăn kèm nước chấm chua ngọt.</li>\r\n  <li><b>Trà sữa, Cà phê bản địa:</b> Từ Cà Phê Trứng Hà Nội, Cà Phê Sữa Đá Sài Gòn, đến các loại chè, tào phớ vỉa hè là thức uống không thể thiếu.</li>\r\n  <li><b>Món ăn vặt:</b> Bánh tráng trộn, gỏi cuốn, bánh bột lọc... là \"linh hồn\" của các buổi xế chiều.</li>\r\n</ol>\r\n\r\n<blockquote>Mỗi món ăn đường phố là một câu chuyện ngắn về cuộc sống: nhanh, gọn và đầy hương vị.</blockquote>\r\n\r\n<h2>3. Mẹo để trải nghiệm Street Food trọn vẹn</h2>\r\n<ul>\r\n  <li><b>Quan sát và Chọn quán:</b> Chọn những quán đông khách, có lượt phục vụ liên tục.</li>\r\n  <li><b>Chuẩn bị tiền mặt:</b> Hầu hết các quán vỉa hè nhỏ chỉ chấp nhận tiền mặt.</li>\r\n  <li><b>Hỏi giá trước:</b> Để tránh hiểu lầm, nhất là khi gọi món theo cân hoặc theo suất.</li>\r\n</ul>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Đừng ngại ngần ngồi xuống một chiếc ghế nhựa nhỏ, hít hà mùi khói bếp và thưởng thức tô phở nóng hổi. Ăn để thấy thành phố chuyển động, để cảm nhận sự sống động và thân thiện của con người nơi đây.</p>'),
(7, NULL, 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?q=80&w=1200&auto=format&fit=crop', 'Kinh nghiệm du lịch một mình an toàn', 'Bí quyết du lịch một mình: đi một mình nhưng không cô đơn, luôn an toàn và tận hưởng trọn vẹn tự do cá nhân.', '<h1>Kinh nghiệm du lịch một mình an toàn & đầy ý nghĩa (Solo Travel)</h1>\r\n<p>Du lịch một mình (Solo Travel) mang đến sự tự do tuyệt đối: tự do quyết định lịch trình, tự do thay đổi ý định, và tự do khám phá chính mình. Tuy nhiên, tự do này luôn đi kèm với trách nhiệm và sự chuẩn bị kỹ lưỡng về an toàn và tâm lý.</p>\r\n\r\n<hr/>\r\n<h2>1. Chuẩn bị cho sự An toàn Tuyệt đối</h2>\r\n<p>Khi đi một mình, bạn là người chịu trách nhiệm 100% cho bản thân. An toàn luôn là ưu tiên hàng đầu.</p>\r\n<ul>\r\n  <li><b>Chia sẻ lịch trình:</b> Luôn chia sẻ thông tin chuyến bay, tên khách sạn, và lịch trình dự kiến cho người thân hoặc bạn bè đáng tin cậy.</li>\r\n  <li><b>Giấy tờ tùy thân & Bảo hiểm:</b> Luôn mang theo bản sao công chứng các giấy tờ quan trọng và mua bảo hiểm du lịch trọn gói.</li>\r\n  <li><b>Quản lý tiền bạc:</b> Không để tất cả tiền và thẻ tín dụng ở một chỗ. Phân tán chúng vào các ví, túi khác nhau.</li>\r\n  <li><b>Số liên hệ khẩn cấp:</b> Lưu sẵn số điện thoại của Đại sứ quán/Lãnh sự quán Việt Nam tại nước sở tại và số điện thoại khẩn cấp địa phương.</li>\r\n</ul>\r\n\r\n<h2>2. Trải nghiệm không cô đơn</h2>\r\n<p>Solo Travel không có nghĩa là bạn phải cô đơn. Đây là cơ hội tuyệt vời để gặp gỡ những người mới.</p>\r\n<ol>\r\n  <li><b>Chọn Hostel hoặc Homestay:</b> Đây là nơi dễ dàng giao lưu với các solo traveler khác.</li>\r\n  <li><b>Tham gia tour trong ngày:</b> Food tour, city tour, hoặc tour mạo hiểm là cách tuyệt vời để có bạn đồng hành tạm thời.</li>\r\n  <li><b>Dùng ứng dụng kết nối:</b> Các ứng dụng như Couchsurfing (chỉ dùng cho mục đích kết nối) hoặc Group du lịch địa phương.</li>\r\n</ol>\r\n\r\n<blockquote>Lưu ý: \"Cô đơn\" là sự thiếu vắng bạn đồng hành. \"Tự do\" là sự hiện diện của chính mình. Du lịch một mình dạy bạn phân biệt hai điều đó.</blockquote>\r\n\r\n<h2>3. Mẹo Quản lý bản thân & Đồ đạc</h2>\r\n<ul>\r\n  <li><b>Đóng gói thông minh:</b> Mang hành lý gọn nhẹ để dễ dàng di chuyển và quản lý đồ đạc cá nhân.</li>\r\n  <li><b>Lưu ý an ninh:</b> Luôn trông chừng đồ đạc khi ăn uống, di chuyển công cộng. Tránh khu vực vắng vẻ vào ban đêm.</li>\r\n  <li><b>Tìm hiểu văn hóa địa phương:</b> Ăn mặc, hành xử phù hợp để tránh thu hút sự chú ý không cần thiết.</li>\r\n</ul>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Du lịch một mình là một món quà bạn tự thưởng cho bản thân. Tự do phải đi cùng trách nhiệm với chính mình. Hãy chuẩn bị kỹ lưỡng, giữ tinh thần cởi mở và tận hưởng trọn vẹn những khám phá nội tại và bên ngoài mà hành trình này mang lại.</p>'),
(8, NULL, 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?q=80&w=1200&auto=format&fit=crop', 'Gợi ý lịch trình 3 ngày ở Đà Nẵng', 'Lịch trình tham quan Đà Nẵng – Hội An 3 ngày 2 đêm: biển, núi, phố cổ và ẩm thực hải sản tươi ngon.', '<h1>Gợi ý lịch trình 3 ngày khám phá Đà Nẵng – Hội An tinh gọn</h1>\r\n<p>Đà Nẵng – Hội An là cặp đôi du lịch hoàn hảo, nơi giao thoa giữa một thành phố biển hiện đại và một thương cảng cổ kính. Lịch trình 72 giờ này sẽ giúp bạn tận dụng tối đa thời gian để trải nghiệm những điểm nhấn quan trọng nhất về biển, núi và văn hóa ẩm thực.</p>\r\n\r\n<hr/>\r\n<h2>Ngày 1: Đà Nẵng hiện đại và Biển Mỹ Khê</h2>\r\n<p>Tập trung khám phá thành phố và thư giãn bên bờ biển.</p>\r\n<ul>\r\n  <li><b>Sáng:</b> Hạ cánh tại Sân bay Đà Nẵng, nhận phòng khách sạn (ưu tiên khu vực gần biển).</li>\r\n  <li><b>Chiều:</b> Tắm biển tại bãi biển <b>Mỹ Khê</b> – một trong những bãi biển đẹp nhất hành tinh. Thưởng thức cà phê hoặc sinh tố dừa ven biển.</li>\r\n  <li><b>Tối:</b> Ăn tối hải sản tươi sống tại khu vực dọc đường Hoàng Sa/Võ Nguyên Giáp. Sau đó, tham quan và mua sắm tại <b>Chợ đêm Helio</b> hoặc dạo bộ dọc bờ sông Hàn.</li>\r\n  <li><b>Đêm:</b> Xem cầu Rồng phun lửa (nếu là tối Thứ Bảy hoặc Chủ Nhật).</li>\r\n</ul>\r\n\r\n<h2>Ngày 2: Khám phá Bà Nà Hills & Ẩm thực miền Trung</h2>\r\n<p>Cả ngày dành cho khu du lịch nổi tiếng trên núi.</p>\r\n<ul>\r\n  <li><b>Sáng & Chiều:</b> Di chuyển đến <b>Bà Nà Hills</b>. Trải nghiệm cáp treo kỷ lục, thăm <b>Cầu Vàng</b> (Golden Bridge) nổi tiếng, Vườn hoa Tình Yêu và Làng Pháp. Nên mua vé online trước để tiết kiệm thời gian.</li>\r\n  <li><b>Tối:</b> Quay về Đà Nẵng. Ăn tối các món đặc sản miền Trung như <b>Bánh tráng cuốn thịt heo hai đầu da</b> hoặc <b>Mì Quảng</b> chính hiệu.</li>\r\n  <li><b>Đêm:</b> Trải nghiệm dịch vụ massage chân hoặc đi dạo bên cầu Tình Yêu.</li>\r\n</ul>\r\n\r\n<blockquote>Cầu Vàng là điểm check-in không thể bỏ qua, nên đi vào sáng sớm để tránh đám đông.</blockquote>\r\n\r\n<h2>Ngày 3: Dấu ấn văn hóa Ngũ Hành Sơn & Phố Cổ Hội An</h2>\r\n<p>Chuyển sang khám phá di sản văn hóa thế giới.</p>\r\n<ul>\r\n  <li><b>Sáng:</b> Tham quan <b>Ngũ Hành Sơn</b> (Marble Mountains). Khám phá các hang động linh thiêng và leo núi ngắm toàn cảnh thành phố.</li>\r\n  <li><b>Trưa:</b> Thưởng thức món Bún Chả Cá Đà Nẵng trước khi di chuyển đến Hội An (khoảng 30-45 phút).</li>\r\n  <li><b>Chiều:</b> Thuê xe đạp (hoặc đi bộ) dạo quanh <b>Phố cổ Hội An</b>. Thăm Chùa Cầu, Nhà cổ Tấn Ký, Hội Quán Phúc Kiến.</li>\r\n  <li><b>Tối:</b> Ăn tối với Cao Lầu, Bánh Bao Bánh Vạc. Tận hưởng không khí lãng mạn của đèn lồng và thả hoa đăng trên sông Hoài.</li>\r\n  <li><b>Đêm:</b> Di chuyển về lại Đà Nẵng (hoặc ra sân bay nếu là chuyến bay đêm).</li>\r\n</ul>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Lịch trình 3 ngày này đã cân bằng giữa nghỉ dưỡng, khám phá thiên nhiên và trải nghiệm văn hóa. Hãy sẵn sàng cho một chuyến đi đầy ắp niềm vui ở miền Trung Việt Nam!</p>'),
(9, NULL, 'https://images.unsplash.com/photo-1526481280698-8fcc13fdc8c7?q=80&w=1200&auto=format&fit=crop', 'Những vật dụng cần có khi đi trekking', 'Checklist chi tiết và thiết yếu cho chuyến trekking: mang đủ – gọn nhẹ – an toàn cho mọi điều kiện thời tiết.', '<h1>Checklist những vật dụng CẦN CÓ khi đi Trekking</h1>\r\n<p>Trekking là cuộc hẹn với thiên nhiên hoang dã, đòi hỏi bạn phải tự chủ và chuẩn bị kỹ lưỡng. Nguyên tắc là: **Mang đúng thứ cần và bỏ lại sự dư thừa**. Một chiếc balo thông minh sẽ quyết định 50% sự thành công và an toàn của chuyến đi.</p>\r\n\r\n<hr/>\r\n<h2>1. Trang bị cá nhân thiết yếu (Safety & Gear)</h2>\r\n<p>Những vật dụng đảm bảo sự an toàn và khả năng di chuyển.</p>\r\n<ul>\r\n  <li><b>Giày Trekking/Leo núi chuyên dụng:</b> Loại chống thấm nước, có độ bám tốt và cổ cao để bảo vệ mắt cá chân.</li>\r\n  <li><b>Quần áo:</b> Ưu tiên đồ khô nhanh, chất liệu thoát mồ hôi. Nên mang theo 1 chiếc áo khoác gió mỏng/áo mưa cá nhân.</li>\r\n  <li><b>Ba lô:</b> Chọn loại có đệm lưng, quai trợ lực tốt, dung tích phù hợp với số ngày đi.</li>\r\n  <li><b>Gậy trekking (Gậy chống):</b> Giảm áp lực lên đầu gối, đặc biệt khi xuống dốc, và giữ thăng bằng.</li>\r\n  <li><b>Đèn pin/Đèn đội đầu:</b> Rất cần thiết cho việc di chuyển trong hang hoặc cắm trại đêm.</li>\r\n</ul>\r\n\r\n<h2>2. Y tế & Dinh dưỡng</h2>\r\n<p>Đừng bao giờ bỏ qua nhóm vật dụng này.</p>\r\n<ol>\r\n  <li><b>Bộ sơ cứu cơ bản:</b> Thuốc giảm đau, thuốc bôi côn trùng cắn, băng gạc, cồn y tế.</li>\r\n  <li><b>Thuốc chống nước:</b> Muối oresol (chống mất nước), vitamin C, thuốc cảm.</li>\r\n  <li><b>Nước uống:</b> Mang đủ bình nước hoặc dụng cụ lọc nước (nếu đi dài ngày).</li>\r\n  <li><b>Thực phẩm năng lượng cao:</b> Thanh năng lượng, chocolate, các loại hạt khô, lương khô.</li>\r\n</ol>\r\n\r\n<blockquote><b>Mẹo an toàn:</b> Luôn giữ một chút thực phẩm và nước sạch dự phòng trong túi nhỏ để dùng trong trường hợp khẩn cấp.</blockquote>\r\n\r\n<h2>3. Dụng cụ sinh tồn & Hỗ trợ</h2>\r\n<ul>\r\n  <li><b>Dao đa năng (Multi-tool):</b> Gồm dao, kéo, kìm nhỏ.</li>\r\n  <li><b>Túi ngủ/Tấm lót (nếu cắm trại):</b> Giúp giữ ấm và ngăn ẩm từ đất.</li>\r\n  <li><b>Bản đồ & La bàn/GPS:</b> Đừng phụ thuộc hoàn toàn vào điện thoại.</li>\r\n  <li><b>Túi chống nước/Bao bọc ba lô:</b> Bảo vệ thiết bị điện tử và quần áo khô ráo.</li>\r\n</ul>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Trekking không chỉ là thử thách sức bền mà còn là thử thách về sự chuẩn bị. Hãy kiểm tra lại checklist ít nhất 3 lần. Mang theo sự tôn trọng với thiên nhiên và một tinh thần sẵn sàng chinh phục!</p>'),
(10, NULL, 'https://images.unsplash.com/photo-1496412705862-e0088f16f791?q=80&w=1200&auto=format&fit=crop', 'Cách chọn khách sạn phù hợp với gia đình', 'Mẹo chọn khách sạn phù hợp cho gia đình có trẻ nhỏ: không gian rộng, tiện ích cho bé và sự thoải mái cho cả nhà.', '<h1>Cách chọn khách sạn phù hợp với gia đình có trẻ nhỏ</h1>\r\n<p>Du lịch cùng trẻ nhỏ là một hành trình thú vị nhưng cũng đầy thử thách. Việc chọn đúng nơi lưu trú sẽ quyết định 80% sự thoải mái và thành công của chuyến đi. Hãy ưu tiên không gian rộng, dịch vụ chu đáo và sự an toàn tuyệt đối cho bé.</p>\r\n\r\n<hr/>\r\n<h2>1. Ưu tiên Không gian & Loại phòng</h2>\r\n<p>Một căn phòng rộng rãi, thoải mái sẽ giúp cả gia đình không bị gò bó.</p>\r\n<ul>\r\n  <li><b>Phòng thông nhau (Connecting Room) hoặc Căn hộ (Apartment):</b> Lý tưởng cho gia đình có hai con trở lên, giúp bố mẹ vừa có không gian riêng tư vừa dễ dàng trông chừng con.</li>\r\n  <li><b>Giường lớn (King size) và Giường phụ (Extra Bed):</b> Xác nhận kích thước giường và khả năng kê thêm giường phụ/nôi em bé (Crib) trước khi đặt.</li>\r\n  <li><b>Nhà bếp nhỏ (Kitchenette):</b> Nếu bé còn nhỏ và cần chuẩn bị đồ ăn riêng, một căn hộ có bếp sẽ là lựa chọn tuyệt vời.</li>\r\n</ul>\r\n\r\n<h2>2. Tiện ích & Dịch vụ dành cho trẻ em</h2>\r\n<p>Nơi lưu trú tốt cho gia đình phải có các dịch vụ hỗ trợ tối đa cho bé.</p>\r\n<ol>\r\n  <li><b>Khu vui chơi trẻ em (Kids Club):</b> Cần có không gian trong nhà và ngoài trời để bé được vận động và giải trí an toàn.</li>\r\n  <li><b>Dịch vụ trông trẻ (Babysitting):</b> Rất cần thiết nếu bố mẹ muốn có một buổi tối riêng tư.</li>\r\n  <li><b>Hồ bơi dành cho trẻ:</b> Hồ bơi cạn, có mái che hoặc khu vực nước nông an toàn.</li>\r\n  <li><b>Tiện ích miễn phí:</b> Ghế ăn dặm, nôi, xe đẩy có sẵn tại khách sạn.</li>\r\n</ol>\r\n\r\n<blockquote>Mẹo: Luôn kiểm tra khu vực Kids Club. Nếu sạch sẽ và có nhân viên trông coi chuyên nghiệp, đó là một điểm cộng lớn.</blockquote>\r\n\r\n<h2>3. Vị trí và An toàn</h2>\r\n<ul>\r\n  <li><b>Vị trí:</b> Ưu tiên gần các tiện ích (siêu thị, hiệu thuốc, bệnh viện) và các điểm tham quan dễ tiếp cận bằng xe đẩy.</li>\r\n  <li><b>Ăn uống:</b> Bữa sáng phải phong phú, có các món dễ ăn cho trẻ (cháo, súp, sữa tươi).</li>\r\n  <li><b>An toàn phòng:</b> Kiểm tra kỹ cửa ban công (có khóa an toàn không), ổ điện, và các góc nhọn trong phòng.</li>\r\n</ul>\r\n\r\n<hr/>\r\n<h2>Kết luận</h2>\r\n<p>Chọn đúng nơi ở để chuyến đi cùng gia đình thêm êm đềm và đáng nhớ. Hãy dành thời gian đọc đánh giá từ các gia đình khác trên các nền tảng đặt phòng để có cái nhìn khách quan nhất về dịch vụ chăm sóc trẻ em.</p>');

-- --------------------------------------------------------

--
-- Table structure for table `promotion`
--

CREATE TABLE `promotion` (
  `promotion_id`   bigint AUTO_INCREMENT PRIMARY KEY,
  `code`           VARCHAR(50) UNIQUE NOT NULL,     -- Mã giảm giá (VD: SUMMER25)
  `discount_amount` DECIMAL(10,2) NOT NULL,       -- Số tiền giảm
  `usage_limit`    INT DEFAULT NULL,                -- Giới hạn số lần dùng (NULL = vô hạn)
  `used_count`     INT DEFAULT 0,                   -- Số lần đã dùng
  `start_date`     DATE NOT NULL,
  `end_date`       DATE NOT NULL,
  `is_active`      BOOLEAN DEFAULT TRUE,            -- Còn hiệu lực không
  `created_at`     DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

--
-- Table structure for table `booking`
--

CREATE TABLE `booking` (
  `booking_id`     bigint AUTO_INCREMENT PRIMARY KEY,
  `user_id`        bigint NOT NULL,
  `room_id`        bigint NOT NULL,
  `check_in_date`  DATE NOT NULL,
  `check_out_date` DATE NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `status` enum('CANCELLED_PERMANENTLY','CONFIRMED','PENDING') NOT NULL,
  `total_price`    DECIMAL(10,2),
  `promotion_id`  bigint DEFAULT NULL,
  `created_at`     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT `fk_booking_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`),
  CONSTRAINT `fk_booking_room` FOREIGN KEY (`room_id`) REFERENCES `room`(`room_id`),
  CONSTRAINT `fk_booking_promotion` FOREIGN KEY (`promotion_id`) REFERENCES `promotion`(`promotion_id`)
) ENGINE=InnoDB;

-- =====================================================================
-- 1. FIX: Bổ sung CHECKED_IN & CHECKED_OUT vào booking.status
--    (Java enum có 5 giá trị nhưng DB chỉ có 3)
-- =====================================================================
ALTER TABLE booking
    MODIFY COLUMN status
        ENUM('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED_PERMANENTLY')
        NOT NULL;
-- --------------------------------------------------------

-- --------------------------------------------------------

--
-- Table structure for table `email_notification`
--

CREATE TABLE `email_notification` (
  `notification_id` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `status` enum('FAILED','PENDING','SENT') NOT NULL,
  `subject` varchar(255) NOT NULL,
  `booking_id` bigint NOT NULL,
  `user_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `flyway_schema_history`
--

CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `flyway_schema_history`
--

INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES
(1, '1', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'root', '2025-10-16 03:26:13', 0, 1),
(2, '2', 'seed restaurants', 'SQL', 'V2__seed_restaurants.sql', -374989434, 'root', '2025-10-16 03:28:06', 6, 1),
(3, '3', 'seed blogs', 'SQL', 'V3__seed_blogs.sql', 1822627392, 'root', '2025-10-16 03:38:49', 302, 1),
(4, '4', 'blog add content excerpt', 'SQL', 'V4__blog_add_content_excerpt.sql', -2080549854, 'root', '2025-10-16 14:45:34', 312, 0);

-- --------------------------------------------------------

--
-- Table structure for table `menu`
--

CREATE TABLE `menu` (
  `menu_id` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  `item_name` varchar(255) NOT NULL,
  `restaurant_id` bigint NOT NULL,
  `price` decimal(12,0) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
INSERT INTO `menu` (`menu_id`, `deleted_at`, `image_url`, `item_name`, `restaurant_id`, `price`) VALUES
(1, NULL, 'https://images.unsplash.com/photo-1520201163981-8aa2f46f4000?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của La Bella Italia', 1, '180000'),
(2, NULL, 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của Sakura Sushi', 2, '240000'),
(3, NULL, 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của El Toro Loco', 3, '150000'),
(4, NULL, 'https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của Bangkok Spice', 4, '160000'),
(5, NULL, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của Le Petit Paris', 5, '320000'),
(6, NULL, 'https://images.unsplash.com/photo-1559339352-2f88a965b38a?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của Harbor Grill', 6, '450000'),
(7, NULL, 'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của Green Garden', 7, '140000'),
(8, NULL, 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của Steakhouse Prime', 8, '650000'),
(9, NULL, 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của Curry Corner', 9, '130000'),
(10, NULL, 'https://images.unsplash.com/photo-1544025162-84f9a6d0a971?q=80&w=1200&auto=format&fit=crop', 'Món đặc biệt của Mandarin Palace', 10, '170000');
--
-- Table structure for table `payment`
--

CREATE TABLE `payment` (
  `payment_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `amount` DECIMAL(15,2) NOT NULL,
  `payment_date` DATETIME(6) NOT NULL,
  `booking_id` BIGINT NOT NULL,
  `transaction_id` VARCHAR(255) NOT NULL,
  `payment_status` ENUM('FAILED','PENDING','SUCCESS') NOT NULL,
  `receipt_url` VARCHAR(1000) DEFAULT NULL,
  CONSTRAINT `fk_payment_booking` FOREIGN KEY (`booking_id`) REFERENCES `booking`(`booking_id`)
);


-- --------------------------------------------------------

--
-- Table structure for table `restaurant`
--

CREATE TABLE `restaurant` (
  `restaurant_id` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `cuisine_type` varchar(100) DEFAULT NULL,
  `opening_hours` varchar(255) DEFAULT NULL,
  `price_range` varchar(64) DEFAULT NULL,
  `promotion_text` varchar(255) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `contact_info` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE restaurant
    ADD COLUMN max_tables    INT     NOT NULL DEFAULT 10    COMMENT 'Số booking tối đa mỗi ca',
    ADD COLUMN has_morning   BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ca sáng 07:00-10:00',
    ADD COLUMN has_lunch     BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ca trưa 11:00-14:00',
    ADD COLUMN has_afternoon BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ca chiều 14:00-17:00',
    ADD COLUMN has_dinner    BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Ca tối 18:00-22:00';

--
-- Dumping data for table `restaurant`
--

INSERT INTO `restaurant` (`restaurant_id`, `deleted_at`, `image_url`, `name`, `cuisine_type`, `opening_hours`) VALUES
(1, NULL, 'https://images.unsplash.com/photo-1520201163981-8aa2f46f4000?q=80&w=1200&auto=format&fit=crop', 'La Bella Italia', 'Italian', '10:00 - 22:00'),
(2, NULL, 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', 'Sakura Sushi', 'Japanese', '11:00 - 21:30'),
(3, NULL, 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?q=80&w=1200&auto=format&fit=crop', 'El Toro Loco', 'Mexican', '10:30 - 23:00'),
(4, NULL, 'https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1200&auto=format&fit=crop', 'Bangkok Spice', 'Thai', '11:00 - 22:00'),
(5, NULL, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?q=80&w=1200&auto=format&fit=crop', 'Le Petit Paris', 'French', '07:30 - 22:00'),
(6, NULL, 'https://images.unsplash.com/photo-1559339352-2f88a965b38a?q=80&w=1200&auto=format&fit=crop', 'Harbor Grill', 'Seafood', '10:00 - 22:00'),
(7, NULL, 'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?q=80&w=1200&auto=format&fit=crop', 'Green Garden', 'Vegetarian', '08:00 - 20:00'),
(8, NULL, 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1200&auto=format&fit=crop', 'Steakhouse Prime', 'Steakhouse', '12:00 - 23:00'),
(9, NULL, 'https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200&auto=format&fit=crop', 'Curry Corner', 'Indian', '11:00 - 21:00'),
(10, NULL, 'https://images.unsplash.com/photo-1544025162-84f9a6d0a971?q=80&w=1200&auto=format&fit=crop', 'Mandarin Palace', 'Chinese', '10:00 - 22:00');

-- =====================================================================
-- 3. Seed ca dựa theo opening_hours của từng nhà hàng
-- =====================================================================

-- La Bella Italia      | 10:00 - 22:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 1;

-- Sakura Sushi         | 11:00 - 21:30 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 2;

-- El Toro Loco         | 10:30 - 23:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 3;

-- Bangkok Spice        | 11:00 - 22:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 4;

-- Le Petit Paris       | 07:30 - 22:00 → sáng, trưa, chiều, tối
UPDATE restaurant SET has_morning = TRUE, has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 5;

-- Harbor Grill         | 10:00 - 22:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 6;

-- Green Garden         | 08:00 - 20:00 → sáng, trưa, chiều, tối
UPDATE restaurant SET has_morning = TRUE, has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 7;

-- Steakhouse Prime     | 12:00 - 23:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 8;

-- Curry Corner         | 11:00 - 21:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 9;

-- Mandarin Palace      | 10:00 - 22:00 → trưa, chiều, tối
UPDATE restaurant SET has_lunch = TRUE, has_afternoon = TRUE, has_dinner = TRUE
WHERE restaurant_id = 10;

-- =====================================================================
-- 4. Tạo bảng restaurant_booking
-- =====================================================================
CREATE TABLE restaurant_booking (
                                    booking_id       BIGINT      NOT NULL AUTO_INCREMENT,
                                    user_id          BIGINT      NOT NULL,
                                    restaurant_id    BIGINT      NOT NULL,
                                    booking_date     DATE        NOT NULL,
                                    booking_shift    ENUM('SANG','TRUA','CHIEU','TOI') NOT NULL
                                        COMMENT 'SANG=07-10 | TRUA=11-14 | CHIEU=14-17 | TOI=18-22',
                                    number_of_guests INT         NOT NULL DEFAULT 1,
                                    special_request  VARCHAR(500)    NULL,
                                    status           ENUM('PENDING','CONFIRMED','CANCELLED')
                                        NOT NULL DEFAULT 'PENDING',
                                    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,
                                    deleted_at       DATETIME        NULL,

                                    PRIMARY KEY (booking_id),
                                    CONSTRAINT fk_rb_user       FOREIGN KEY (user_id)
                                        REFERENCES users(user_id),
                                    CONSTRAINT fk_rb_restaurant FOREIGN KEY (restaurant_id)
                                        REFERENCES restaurant(restaurant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =====================================================================
-- 5. Indexes tối ưu hiệu suất truy vấn
-- =====================================================================

-- Kiểm tra capacity: nhà hàng + ngày + ca
CREATE INDEX idx_rb_restaurant_date_shift
    ON restaurant_booking (restaurant_id, booking_date, booking_shift);

-- Lịch sử đặt bàn của user
CREATE INDEX idx_rb_user
    ON restaurant_booking (user_id);

-- Filter theo trạng thái (admin/receptionist)
CREATE INDEX idx_rb_status
    ON restaurant_booking (status);

-- Xóa mềm
CREATE INDEX idx_rb_deleted_at
    ON restaurant_booking (deleted_at);

-- V16: Thêm cột lý do hủy vào bảng restaurant_booking
ALTER TABLE restaurant_booking
    ADD COLUMN cancel_reason VARCHAR(500) NULL COMMENT 'Lý do hủy đặt bàn'
        AFTER special_request;

-- --------------------------------------------------------



-- --------------------------------------------------------

--
-- Table structure for table `setting`
--

CREATE TABLE `setting` (
  `setting_id` bigint NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `priority` int NOT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL,
  `type` enum('MASTER_DATA','SYSTEM_CONFIG','USER_ROLE') NOT NULL,
  `value` varchar(1000) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------


--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `avatar_url`, `deleted_at`, `email`, `password`, `role_id`, `status`,`phone`, `address`, `username`) VALUES
(1, NULL, NULL, 'nguyenhoang173073@gmail.com', '$2a$10$p31zEEVaGcvhHnaY0bXuHusEfca6UbyHhMLmlWXoqxWIgWqVt3HAq', 'ADMIN', 'ACTIVE','012345678','Ha Noi', 'phuchoang'),
(2, NULL, NULL, 'hoangnam242009@gmail.com', '$2a$10$p31zEEVaGcvhHnaY0bXuHusEfca6UbyHhMLmlWXoqxWIgWqVt3HAq', 'CUSTOMER', 'ACTIVE','012345679','Ha Noi', 'hoangnam'),
(3, NULL, NULL, 'nguyenhoang300281@gmail.com', '$2a$10$p31zEEVaGcvhHnaY0bXuHusEfca6UbyHhMLmlWXoqxWIgWqVt3HAq', 'RECEPTIONIST', 'ACTIVE','012345676','Ha Noi', 'namhoang');
--
-- Indexes for dumped tables
--
-- --------------------------------------------------------

INSERT INTO `booking` (`booking_id`,`user_id`,`room_id`,`check_in_date`,`check_out_date`,`deleted_at`,`status`,`total_price`,`promotion_id`,`created_at`,`updated_at`) VALUES (1,2,1,'2025-11-08','2025-11-09',NULL,'CANCELLED_PERMANENTLY',500000.00,NULL,'2025-11-08 12:19:52','2025-11-08 12:22:02');
INSERT INTO `booking` (`booking_id`,`user_id`,`room_id`,`check_in_date`,`check_out_date`,`deleted_at`,`status`,`total_price`,`promotion_id`,`created_at`,`updated_at`) VALUES (2,2,5,'2025-11-08','2025-11-09',NULL,'CONFIRMED',800000.00,NULL,'2025-11-08 12:20:27','2025-11-08 12:21:04');

INSERT INTO `payment` (`payment_id`,`amount`,`payment_date`,`booking_id`,`transaction_id`,`payment_status`,`receipt_url`) VALUES (1,500000.00,'2025-11-08 19:21:20.151092',1,'1_1','FAILED',NULL);
INSERT INTO `payment` (`payment_id`,`amount`,`payment_date`,`booking_id`,`transaction_id`,`payment_status`,`receipt_url`) VALUES (2,800000.00,'2025-11-08 19:20:29.516668',2,'2_2','SUCCESS',NULL);
--
-- Indexes for table `amenity`
--
ALTER TABLE `amenity`
  ADD PRIMARY KEY (`amenity_id`);

--
-- Indexes for table `blog`
--
ALTER TABLE `blog`
  ADD PRIMARY KEY (`blog_id`);

--
-- Indexes for table `email_notification`
--
ALTER TABLE `email_notification`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `FKqy5kfo2h4bs242djqa1rc74c2` (`booking_id`),
  ADD KEY `FKl0dyo3f66ct9v9lajxtbwqkm4` (`user_id`);

--
-- Indexes for table `flyway_schema_history`
--
ALTER TABLE `flyway_schema_history`
  ADD PRIMARY KEY (`installed_rank`),
  ADD KEY `flyway_schema_history_s_idx` (`success`);

--
-- Indexes for table `menu`
--
ALTER TABLE `menu`
  ADD PRIMARY KEY (`menu_id`),
  ADD KEY `FKblwdtxevpl4mrds8a12q0ohu6` (`restaurant_id`);

--
-- Indexes for table `restaurant`
--
ALTER TABLE `restaurant`
  ADD PRIMARY KEY (`restaurant_id`);

--
-- Indexes for table `setting`
--
ALTER TABLE `setting`
  ADD PRIMARY KEY (`setting_id`);

--
-- AUTO_INCREMENT for table `amenity`
--
ALTER TABLE `amenity`
  MODIFY `amenity_id` bigint NOT NULL AUTO_INCREMENT;
  
INSERT INTO `amenity` (`amenity_id`,`deleted_at`,`image_url`,`amenity_name`,`description`) VALUES (1,NULL,'https://res.cloudinary.com/dpq72ssgq/image/upload/v1762606035/hotel/amenities/f6mg95jubnmsl2yh3zoj.jpg','Gym in hotel','<p>Ảnh chụp một ph&ograve;ng tập thể dục trong nh&agrave; c&oacute; thiết kế hiện đại, s&agrave;n gỗ s&aacute;ng m&agrave;u, &aacute;nh s&aacute;ng tự nhi&ecirc;n chiếu qua c&aacute;c cửa sổ lớn. Trong ph&ograve;ng c&oacute; nhiều m&aacute;y chạy bộ v&agrave; m&aacute;y tập cardio được xếp ngay ngắn th&agrave;nh h&agrave;ng dọc theo bức tường c&oacute; cửa sổ. Ở giữa, một người đ&agrave;n &ocirc;ng mặc &aacute;o s&aacute;t n&aacute;ch m&agrave;u tối v&agrave; quần ngắn đang chạy tr&ecirc;n m&aacute;y chạy bộ, d&aacute;ng người thẳng, thể hiện tinh thần năng động. Kh&ocirc;ng gian gọn g&agrave;ng, sạch sẽ, c&aacute;c thiết bị đều hiện đại với m&agrave;n h&igrave;nh cảm ứng v&agrave; tay cầm chắc chắn. Ph&iacute;a sau c&oacute; logo của kh&aacute;ch sạn hiển thị tr&ecirc;n m&agrave;n h&igrave;nh treo tường, cho thấy đ&acirc;y l&agrave; khu tập thể h&igrave;nh trong khu&ocirc;n vi&ecirc;n kh&aacute;ch sạn cao cấp. Tổng thể mang lại cảm gi&aacute;c chuy&ecirc;n nghiệp, s&aacute;ng sủa v&agrave; tr&agrave;n đầy năng lượng, ph&ugrave; hợp cho tập luyện cardio v&agrave; duy tr&igrave; lối sống l&agrave;nh mạnh.</p>');

--
-- AUTO_INCREMENT for table `blog`
--
ALTER TABLE `blog`
  MODIFY `blog_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `email_notification`
--
ALTER TABLE `email_notification`
  MODIFY `notification_id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `menu`
--
ALTER TABLE `menu`
  MODIFY `menu_id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `restaurant`
--
ALTER TABLE `restaurant`
  MODIFY `restaurant_id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

  
--
-- AUTO_INCREMENT for table `setting`
--
ALTER TABLE `setting`
  MODIFY `setting_id` bigint NOT NULL AUTO_INCREMENT;



--
-- Constraints for table `email_notification`
--
ALTER TABLE `email_notification`
  ADD CONSTRAINT `FKl0dyo3f66ct9v9lajxtbwqkm4` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `FKqy5kfo2h4bs242djqa1rc74c2` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`);

CREATE TABLE `feedback` (
    `feedback_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `category` ENUM('BOOKING_SERVICE','RESTAURANT','BOTH') NOT NULL,
    `restaurant_id` BIGINT,
    `room_type_id` BIGINT,
    `content` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    CONSTRAINT `fk_feedback_user` 
        FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`),
    CONSTRAINT `fk_feedback_restaurant` 
        FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant`(`restaurant_id`),
    CONSTRAINT `fk_feedback_room_type` 
        FOREIGN KEY (`room_type_id`) REFERENCES `room_type`(`room_type_id`)
);
--

CREATE TABLE `room_type_review` (
    `review_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `room_type_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `rating` INT NOT NULL,
    `comment` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    CONSTRAINT `uk_room_type_review_room_user` UNIQUE (`room_type_id`, `user_id`),
    CONSTRAINT `fk_room_type_review_room_type` FOREIGN KEY (`room_type_id`)
        REFERENCES `room_type` (`room_type_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_room_type_review_user` FOREIGN KEY (`user_id`)
        REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
