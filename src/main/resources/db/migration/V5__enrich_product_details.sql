-- ==============================================================================

-- Migration V5: Chuẩn hóa và làm giàu dữ liệu chi tiết sản phẩm (Enrich Products)

-- 1. Bổ sung các bước Hướng dẫn sử dụng (Bước 1, 2, 3, 4) chuẩn da liễu

-- 2. Mở rộng danh sách Hoạt chất chính kèm công dụng chuyên sâu

-- ==============================================================================

UPDATE `products` SET `key_ingredients` = '["Glycerin - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h","Niacinamide (Vitamin B3) - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","Panthenol (Pro-Vitamin B5) - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Dầu hạnh nhân ngọt (Sweet Almond Oil) - Bổ sung acid béo tự nhiên, nuôi dưỡng da mềm mại và đàn hồi"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 1;

UPDATE `products` SET `key_ingredients` = '["Bơ hạt mỡ hữu cơ (Shea Butter) - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ","Glycerin - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h","Dầu đậu nành - Giàu dưỡng chất và acid béo giúp làm mềm biểu bì da","Vitamin E - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 2;

UPDATE `products` SET `key_ingredients` = '["Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","Panthenol - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Glycerin - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h","Tocopherol (Vitamin E) - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 3;

UPDATE `products` SET `key_ingredients` = '["Niacinamide (Vitamin B3) - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","Panthenol (Pro-Vitamin B5) - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Glycerin cấp ẩm - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 4;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất hoa cúc hữu cơ (Organic Calendula) - Kháng khuẩn tự nhiên, làm dịu da nhạy cảm và ngừa kích ứng","Panthenol - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Glycerin - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 5;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất hoa sen Nhật Bản - Chống oxy hóa cao, làm dịu da mệt mỏi và dưỡng sáng thanh nhã","Nước gạo lên men - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dưỡng chất Nutritive Serum - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 6;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất trà xanh Matcha Nhật Bản - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hương hoa anh đào (Cherry Blossom) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hệ dưỡng chất Bio-Nourish - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 7;

UPDATE `products` SET `key_ingredients` = '["Bio-Cellular Complex - Nuôi dưỡng nang tóc khỏe mạnh, ngăn ngừa đứt gãy từ chân tóc","Serum dưỡng sâu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Sodium Gluconate - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 8;

UPDATE `products` SET `key_ingredients` = '["Keratin Repair Actives - Hàn gắn liên kết protein tóc bị đứt gãy, giúp tóc chắc khỏe bóng mượt","Nutri-Keratin - Hàn gắn liên kết protein tóc bị đứt gãy, giúp tóc chắc khỏe bóng mượt","Glycerin dưỡng ẩm - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 9;

UPDATE `products` SET `key_ingredients` = '["Hydro Wax dưỡng chất - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glycerin - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h","Bơ hạt mỡ hữu cơ - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 10;

UPDATE `products` SET `key_ingredients` = '["Eucerit độc quyền Nivea - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Panthenol - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Glycerin - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 11;

UPDATE `products` SET `key_ingredients` = '["00x Vitamin A - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vitamin C, E, B3, B9, Pro-B5 - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất Goji Berry, Lựu, Bơ, Chanh vàng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 12;

UPDATE `products` SET `key_ingredients` = '["0x Vitamin C cô đặc - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vitamin E tinh khiết - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do","Màng lọc UV - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 13;

UPDATE `products` SET `key_ingredients` = '["Q10 & Collagen - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dẫn xuất Vitamin C nguyên chất - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glycerin cấp ẩm - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 14;

UPDATE `products` SET `key_ingredients` = '["Công thức Pro-Vitamin B5 - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chất dưỡng ẩm Moisture Renewal - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chất chống oxy hóa Histidine - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 15;

UPDATE `products` SET `key_ingredients` = '["Pro-Vitamin B5 Complex - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dưỡng chất Keratin Damage Blockers - Hàn gắn liên kết protein tóc bị đứt gãy, giúp tóc chắc khỏe bóng mượt"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 16;

UPDATE `products` SET `key_ingredients` = '["Kẽm Pyrithione (ZPT) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Công thức Pro-V - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Tinh dầu làm mềm tóc - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 17;

UPDATE `products` SET `key_ingredients` = '["Pro-Vitamin B5 Liposome - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Viên nang Serum Vàng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glycerin đậm đặc - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Sau khi gội đầu, vuốt nhẹ cho tóc ráo bớt nước.
Bước 2: Lấy lượng kem xả/kem ủ vừa đủ thoa đều từ thân tóc đến ngọn tóc (cách chân tóc 3-5cm để tránh bết da đầu).
Bước 3: Mát-xa nhẹ nhàng các lọn tóc và ủ trong 2-5 phút (hoặc 10-15 phút đối với kem ủ sâu).
Bước 4: Xả sạch kỹ lại bằng nước mát để khép biểu bì tóc, giúp tóc bóng mượt bồng bềnh.' WHERE `id_product` = 18;

UPDATE `products` SET `key_ingredients` = '["Pro-Vitamin Complex - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Amino Acids - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dầu dưỡng chất tự nhiên - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Sau khi gội đầu, vuốt nhẹ cho tóc ráo bớt nước.
Bước 2: Lấy lượng kem xả/kem ủ vừa đủ thoa đều từ thân tóc đến ngọn tóc (cách chân tóc 3-5cm để tránh bết da đầu).
Bước 3: Mát-xa nhẹ nhàng các lọn tóc và ủ trong 2-5 phút (hoặc 10-15 phút đối với kem ủ sâu).
Bước 4: Xả sạch kỹ lại bằng nước mát để khép biểu bì tóc, giúp tóc bóng mượt bồng bềnh.' WHERE `id_product` = 19;

UPDATE `products` SET `key_ingredients` = '["Công nghệ TRESplex Rebond - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Protein thủy phân - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Keratin phục hồi - Hàn gắn liên kết protein tóc bị đứt gãy, giúp tóc chắc khỏe bóng mượt"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 20;

UPDATE `products` SET `key_ingredients` = '["Keratin protein đậm đặc - Hàn gắn liên kết protein tóc bị đứt gãy, giúp tóc chắc khỏe bóng mượt","Tinh dầu Argan hữu cơ Maroc - Cung cấp acid béo và vitamin E nuôi dưỡng sợi tóc suôn mượt vào nếp"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ tóc và da đầu bằng nước ấm.
Bước 2: Lấy lượng dầu gội vừa đủ ra tay, xoa tạo bọt kỹ với một ít nước.
Bước 3: Thoa đều lên da đầu, dùng đầu ngón tay mát-xa nhẹ nhàng theo hình tròn trong 2-3 phút để làm sạch bụi bẩn và kích thích tuần hoàn.
Bước 4: Xả sạch hoàn toàn với nước mát. Khuyên dùng kết hợp dầu xả cùng dòng từ thân đến ngọn tóc để đạt hiệu quả mềm mượt tối đa.' WHERE `id_product` = 21;

UPDATE `products` SET `key_ingredients` = '["Tinh dầu Argan Maroc - Cung cấp acid béo và vitamin E nuôi dưỡng sợi tóc suôn mượt vào nếp","Keratin thủy phân - Hàn gắn liên kết protein tóc bị đứt gãy, giúp tóc chắc khỏe bóng mượt","Dimethicone bảo vệ tóc - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Sau khi gội đầu, vuốt nhẹ cho tóc ráo bớt nước.
Bước 2: Lấy lượng kem xả/kem ủ vừa đủ thoa đều từ thân tóc đến ngọn tóc (cách chân tóc 3-5cm để tránh bết da đầu).
Bước 3: Mát-xa nhẹ nhàng các lọn tóc và ủ trong 2-5 phút (hoặc 10-15 phút đối với kem ủ sâu).
Bước 4: Xả sạch kỹ lại bằng nước mát để khép biểu bì tóc, giúp tóc bóng mượt bồng bềnh.' WHERE `id_product` = 22;

UPDATE `products` SET `key_ingredients` = '["Dầu Argan Marula nguyên chất - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hạt dầu Keratin siêu nhẹ - Hàn gắn liên kết protein tóc bị đứt gãy, giúp tóc chắc khỏe bóng mượt"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 23;

UPDATE `products` SET `key_ingredients` = '["Công nghệ TRESplex - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Protein gạo thủy phân - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Lipid nuôi dưỡng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Sau khi gội đầu, vuốt nhẹ cho tóc ráo bớt nước.
Bước 2: Lấy lượng kem xả/kem ủ vừa đủ thoa đều từ thân tóc đến ngọn tóc (cách chân tóc 3-5cm để tránh bết da đầu).
Bước 3: Mát-xa nhẹ nhàng các lọn tóc và ủ trong 2-5 phút (hoặc 10-15 phút đối với kem ủ sâu).
Bước 4: Xả sạch kỹ lại bằng nước mát để khép biểu bì tóc, giúp tóc bóng mượt bồng bềnh.' WHERE `id_product` = 24;

UPDATE `products` SET `key_ingredients` = '["Keratin đậm đặc - Hàn gắn liên kết protein tóc bị đứt gãy, giúp tóc chắc khỏe bóng mượt","Dầu hạt mỡ Argan - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dưỡng chất Marula - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Sau khi gội đầu, vuốt nhẹ cho tóc ráo bớt nước.
Bước 2: Lấy lượng kem xả/kem ủ vừa đủ thoa đều từ thân tóc đến ngọn tóc (cách chân tóc 3-5cm để tránh bết da đầu).
Bước 3: Mát-xa nhẹ nhàng các lọn tóc và ủ trong 2-5 phút (hoặc 10-15 phút đối với kem ủ sâu).
Bước 4: Xả sạch kỹ lại bằng nước mát để khép biểu bì tóc, giúp tóc bóng mượt bồng bềnh.' WHERE `id_product` = 25;

UPDATE `products` SET `key_ingredients` = '["00% White Petrolatum tinh khiết - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 26;

UPDATE `products` SET `key_ingredients` = '["GlutaGlow độc quyền - Làm sáng da gấp 10 lần Vitamin C, giảm thâm sạm vượt bậc","Hyaluron (Hyaluronic Acid) - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","0x Pro-Retinol / Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 27;

UPDATE `products` SET `key_ingredients` = '["00% Pure Cocoa & Shea Butter - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vi giọt sáp Vaseline Jelly - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glycerin - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 28;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất Vanilla - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vi giọt sáp Vaseline Jelly - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hệ dưỡng ẩm Intensive Care - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 29;

UPDATE `products` SET `key_ingredients` = '["Bộ màng lọc chống nắng SPF 50+ PA++++ - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Niacinamide (Vitamin B3) - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","Chiết xuất bạc hà mát lạnh - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Hoàn thành các bước dưỡng ẩm cơ bản vào buổi sáng.
Bước 2: Lắc đều sản phẩm (đối với dạng sữa). Lấy một lượng vừa đủ (khoảng 2 đốt ngón tay hoặc cỡ đồng xu).
Bước 3: Chấm đều lên các vùng trán, má, cằm, cổ và vỗ nhẹ đều khắp mặt cho kem tiệp vào da (không chà xát mạnh).
Bước 4: Thoa trước khi ra nắng ít nhất 15-20 phút. Thoa lại sau mỗi 2-3 giờ nếu tiếp xúc lâu với ánh nắng hoặc ra nhiều mồ hôi.' WHERE `id_product` = 30;

UPDATE `products` SET `key_ingredients` = '["% Salicylic Acid (BHA) - Thẩm thấu sâu vào lỗ chân lông, loại bỏ bã nhờn và tế bào chết tích tụ","Glycolic & Lactic Acid (AHA) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Ceramides thiết yếu (1, 3, 6-II) - Tái tạo và củng cố lớp màng lipid sinh học bảo vệ da","Niacinamide (Vitamin B3) - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 31;

UPDATE `products` SET `key_ingredients` = '["Ceramides thiết yếu (1, 3, 6-II) - Tái tạo và củng cố lớp màng lipid sinh học bảo vệ da","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Công nghệ phân phối dưỡng chất MVE - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 32;

UPDATE `products` SET `key_ingredients` = '["Công nghệ hạt Micellar - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Ceramides thiết yếu - Tái tạo và củng cố lớp màng lipid sinh học bảo vệ da","Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da"]', `usage_instruction` = 'Bước 1: Thấm một lượng nước tẩy trang vừa đủ ướt đẫm miếng bông cotton chuyên dụng.
Bước 2: Áp miếng bông lên mắt và môi trong 5-10 giây để hòa tan lớp trang điểm cứng đầu, sau đó vuốt nhẹ nhàng.
Bước 3: Dùng miếng bông mới lau đều khắp mặt từ trong ra ngoài và từ dưới lên trên đến khi bông sạch hoàn toàn.
Bước 4: Rửa sạch lại với nước và sữa rửa mặt (Double Cleansing) để lỗ chân lông thông thoáng hoàn toàn.' WHERE `id_product` = 33;

UPDATE `products` SET `key_ingredients` = '["Zinc Oxide & Titanium Dioxide - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Ceramides thiết yếu - Tái tạo và củng cố lớp màng lipid sinh học bảo vệ da","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da"]', `usage_instruction` = 'Bước 1: Hoàn thành các bước dưỡng ẩm cơ bản vào buổi sáng.
Bước 2: Lắc đều sản phẩm (đối với dạng sữa). Lấy một lượng vừa đủ (khoảng 2 đốt ngón tay hoặc cỡ đồng xu).
Bước 3: Chấm đều lên các vùng trán, má, cằm, cổ và vỗ nhẹ đều khắp mặt cho kem tiệp vào da (không chà xát mạnh).
Bước 4: Thoa trước khi ra nắng ít nhất 15-20 phút. Thoa lại sau mỗi 2-3 giờ nếu tiếp xúc lâu với ánh nắng hoặc ra nhiều mồ hôi.' WHERE `id_product` = 34;

UPDATE `products` SET `key_ingredients` = '["Ceramides thiết yếu (1, 3, 6-II) - Tái tạo và củng cố lớp màng lipid sinh học bảo vệ da","Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 35;

UPDATE `products` SET `key_ingredients` = '["Nước cất hoa hồng hữu cơ Cao Bằng - Cấp ẩm tức thì, se khít lỗ chân lông và làm mịn bề mặt da","Hyaluronic Acid phân tử nhỏ - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Pentavitin - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Axit Amin tự nhiên - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 36;

UPDATE `products` SET `key_ingredients` = '["Cánh hoa hồng tươi Cao Bằng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất nha đam - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng"]', `usage_instruction` = 'Bước 1: Sau khi gội đầu, vuốt nhẹ cho tóc ráo bớt nước.
Bước 2: Lấy lượng kem xả/kem ủ vừa đủ thoa đều từ thân tóc đến ngọn tóc (cách chân tóc 3-5cm để tránh bết da đầu).
Bước 3: Mát-xa nhẹ nhàng các lọn tóc và ủ trong 2-5 phút (hoặc 10-15 phút đối với kem ủ sâu).
Bước 4: Xả sạch kỹ lại bằng nước mát để khép biểu bì tóc, giúp tóc bóng mượt bồng bềnh.' WHERE `id_product` = 37;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất bí đao - Thanh nhiệt, làm dịu các nốt mụn sưng đỏ và kiểm soát bã nhờn","Tinh dầu tràm trà (Tea Tree Oil) - Kháng khuẩn mạnh mẽ, làm sạch và gom cồi mụn nhanh chóng","Chiết xuất rau má (Centella) - Kích thích tăng sinh collagen, làm dịu da và phục hồi sau mụn","Betaine - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Thấm một lượng nước tẩy trang vừa đủ ướt đẫm miếng bông cotton chuyên dụng.
Bước 2: Áp miếng bông lên mắt và môi trong 5-10 giây để hòa tan lớp trang điểm cứng đầu, sau đó vuốt nhẹ nhàng.
Bước 3: Dùng miếng bông mới lau đều khắp mặt từ trong ra ngoài và từ dưới lên trên đến khi bông sạch hoàn toàn.
Bước 4: Rửa sạch lại với nước và sữa rửa mặt (Double Cleansing) để lỗ chân lông thông thoáng hoàn toàn.' WHERE `id_product` = 38;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất hoa sen Đồng Tháp - Chống oxy hóa cao, làm dịu da mệt mỏi và dưỡng sáng thanh nhã","Vitamin B12 - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Beta-Glucan - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hạt làm sạch Micellar - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Thấm một lượng nước tẩy trang vừa đủ ướt đẫm miếng bông cotton chuyên dụng.
Bước 2: Áp miếng bông lên mắt và môi trong 5-10 giây để hòa tan lớp trang điểm cứng đầu, sau đó vuốt nhẹ nhàng.
Bước 3: Dùng miếng bông mới lau đều khắp mặt từ trong ra ngoài và từ dưới lên trên đến khi bông sạch hoàn toàn.
Bước 4: Rửa sạch lại với nước và sữa rửa mặt (Double Cleansing) để lỗ chân lông thông thoáng hoàn toàn.' WHERE `id_product` = 39;

UPDATE `products` SET `key_ingredients` = '["Màng lọc chống nắng thế hệ mới - Quang phổ rộng bảo vệ da toàn diện trước tia UVA, UVB và ánh sáng xanh","Chiết xuất bí đao - Thanh nhiệt, làm dịu các nốt mụn sưng đỏ và kiểm soát bã nhờn","Syn-Actu (kháng viêm) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da"]', `usage_instruction` = 'Bước 1: Hoàn thành các bước dưỡng ẩm cơ bản vào buổi sáng.
Bước 2: Lắc đều sản phẩm (đối với dạng sữa). Lấy một lượng vừa đủ (khoảng 2 đốt ngón tay hoặc cỡ đồng xu).
Bước 3: Chấm đều lên các vùng trán, má, cằm, cổ và vỗ nhẹ đều khắp mặt cho kem tiệp vào da (không chà xát mạnh).
Bước 4: Thoa trước khi ra nắng ít nhất 15-20 phút. Thoa lại sau mỗi 2-3 giờ nếu tiếp xúc lâu với ánh nắng hoặc ra nhiều mồ hôi.' WHERE `id_product` = 40;

UPDATE `products` SET `key_ingredients` = '["Màng lọc quang phổ rộng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất hoa sen - Chống oxy hóa cao, làm dịu da mệt mỏi và dưỡng sáng thanh nhã","Vitamin B12 - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bột ngọc trai tạo hiệu ứng sáng mịn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Hoàn thành các bước dưỡng ẩm cơ bản vào buổi sáng.
Bước 2: Lắc đều sản phẩm (đối với dạng sữa). Lấy một lượng vừa đủ (khoảng 2 đốt ngón tay hoặc cỡ đồng xu).
Bước 3: Chấm đều lên các vùng trán, má, cằm, cổ và vỗ nhẹ đều khắp mặt cho kem tiệp vào da (không chà xát mạnh).
Bước 4: Thoa trước khi ra nắng ít nhất 15-20 phút. Thoa lại sau mỗi 2-3 giờ nếu tiếp xúc lâu với ánh nắng hoặc ra nhiều mồ hôi.' WHERE `id_product` = 41;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất bí đao - Thanh nhiệt, làm dịu các nốt mụn sưng đỏ và kiểm soát bã nhờn","Rau má - Kích thích tăng sinh collagen, làm dịu da và phục hồi sau mụn","Tinh dầu tràm trà - Kháng khuẩn mạnh mẽ, làm sạch và gom cồi mụn nhanh chóng","Vitamin B3 (Niacinamide) - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 42;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất hoa sen tươi - Chống oxy hóa cao, làm dịu da mệt mỏi và dưỡng sáng thanh nhã","Vitamin B12 - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glycerin thực vật - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h","Axit Amin dịu nhẹ - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 43;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất nghệ Hưng Yên giàu Curcumin - Mờ thâm mụn, dưỡng sáng da và chống oxy hóa vượt trội","% AHA (Lactic Acid) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Betaine - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dầu hạt cà rốt - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 44;

UPDATE `products` SET `key_ingredients` = '["Hạt cà phê Đắk Lắk xay siêu mịn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bơ ca cao Tiền Giang - Khóa ẩm sâu, ngăn ngừa thoát nước và phục hồi da khô ráp","Dầu hoa rum - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vitamin E - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do"]', `usage_instruction` = 'Bước 1: Làm sạch vùng da cần chăm sóc với nước ấm để làm mềm lớp sừng già cỗi.
Bước 2: Lấy một lượng sản phẩm vừa đủ thoa đều lên vùng da còn ẩm.
Bước 3: Mát-xa nhẹ nhàng theo chuyển động tròn trong 2-3 phút, tập trung vùng da sần sùi (khuỷu tay, đầu gối, cánh mũi).
Bước 4: Rửa sạch lại với nước và thoa ngay kem dưỡng ẩm/dưỡng thể. Dùng đều đặn 1-2 lần/tuần, không lạm dụng.' WHERE `id_product` = 45;

UPDATE `products` SET `key_ingredients` = '["Cà phê Đắk Lắk - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bơ hạt mỡ - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ","Dầu mắc-ca - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm sạch vùng da cần chăm sóc với nước ấm để làm mềm lớp sừng già cỗi.
Bước 2: Lấy một lượng sản phẩm vừa đủ thoa đều lên vùng da còn ẩm.
Bước 3: Mát-xa nhẹ nhàng theo chuyển động tròn trong 2-3 phút, tập trung vùng da sần sùi (khuỷu tay, đầu gối, cánh mũi).
Bước 4: Rửa sạch lại với nước và thoa ngay kem dưỡng ẩm/dưỡng thể. Dùng đều đặn 1-2 lần/tuần, không lạm dụng.' WHERE `id_product` = 46;

UPDATE `products` SET `key_ingredients` = '["Cà phê Robusta Đắk Lắk nguyên chất - Chống oxy hóa cao, lấy sạch tế bào sừng già cỗi nhẹ nhàng","Bơ ca cao Tiền Giang - Khóa ẩm sâu, ngăn ngừa thoát nước và phục hồi da khô ráp","Dầu đậu nành - Giàu dưỡng chất và acid béo giúp làm mềm biểu bì da"]', `usage_instruction` = 'Bước 1: Làm sạch vùng da cần chăm sóc với nước ấm để làm mềm lớp sừng già cỗi.
Bước 2: Lấy một lượng sản phẩm vừa đủ thoa đều lên vùng da còn ẩm.
Bước 3: Mát-xa nhẹ nhàng theo chuyển động tròn trong 2-3 phút, tập trung vùng da sần sùi (khuỷu tay, đầu gối, cánh mũi).
Bước 4: Rửa sạch lại với nước và thoa ngay kem dưỡng ẩm/dưỡng thể. Dùng đều đặn 1-2 lần/tuần, không lạm dụng.' WHERE `id_product` = 47;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất bí đao - Thanh nhiệt, làm dịu các nốt mụn sưng đỏ và kiểm soát bã nhờn","Tinh dầu tràm trà - Kháng khuẩn mạnh mẽ, làm sạch và gom cồi mụn nhanh chóng","% Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","Chiết xuất rau má - Kích thích tăng sinh collagen, làm dịu da và phục hồi sau mụn"]', `usage_instruction` = 'Bước 1: Rửa mặt thật sạch và dùng khăn bông mềm thấm ráo nước.
Bước 2: Đổ 3-4 giọt toner ra lòng bàn tay hoặc thấm ướt bông tẩy trang mỏng.
Bước 3: Vỗ nhẹ nhàng đều khắp mặt và cổ để cân bằng độ pH và mở đường cho các bước dưỡng chất tiếp theo.
Bước 4: Tiếp tục chu trình dưỡng da với serum và kem dưỡng ẩm ngay khi da còn hơi ẩm.' WHERE `id_product` = 48;

UPDATE `products` SET `key_ingredients` = '["Nước cất hoa sen Đồng Tháp - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Beta-Glucan - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vitamin B12 - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng"]', `usage_instruction` = 'Bước 1: Rửa mặt thật sạch và dùng khăn bông mềm thấm ráo nước.
Bước 2: Đổ 3-4 giọt toner ra lòng bàn tay hoặc thấm ướt bông tẩy trang mỏng.
Bước 3: Vỗ nhẹ nhàng đều khắp mặt và cổ để cân bằng độ pH và mở đường cho các bước dưỡng chất tiếp theo.
Bước 4: Tiếp tục chu trình dưỡng da với serum và kem dưỡng ẩm ngay khi da còn hơi ẩm.' WHERE `id_product` = 49;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất trà xanh Jeju - Kháng viêm, làm dịu da và chống lại tác nhân oxy hóa từ môi trường","Chiết xuất rau má Centella - Kích thích tăng sinh collagen, làm dịu da và phục hồi sau mụn","Dầu hạt hướng dương - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Hoàn thành các bước dưỡng ẩm cơ bản vào buổi sáng.
Bước 2: Lắc đều sản phẩm (đối với dạng sữa). Lấy một lượng vừa đủ (khoảng 2 đốt ngón tay hoặc cỡ đồng xu).
Bước 3: Chấm đều lên các vùng trán, má, cằm, cổ và vỗ nhẹ đều khắp mặt cho kem tiệp vào da (không chà xát mạnh).
Bước 4: Thoa trước khi ra nắng ít nhất 15-20 phút. Thoa lại sau mỗi 2-3 giờ nếu tiếp xúc lâu với ánh nắng hoặc ra nhiều mồ hôi.' WHERE `id_product` = 50;

UPDATE `products` SET `key_ingredients` = '["Bơ hạt mỡ - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ","Dầu olive - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất thảo mộc đảo Jeju - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tắm sạch cơ thể bằng nước ấm để mở lỗ chân lông và thấm khô nhẹ bằng khăn mềm.
Bước 2: Lấy một lượng sữa dưỡng thể/kem vừa đủ ra lòng bàn tay.
Bước 3: Thoa đều lên khắp các vùng da trên cơ thể (cánh tay, chân, cổ, vùng da khô ráp) khi da còn hơi ẩm để tăng hấp thụ.
Bước 4: Mát-xa nhẹ nhàng theo chuyển động tròn cho đến khi kem thẩm thấu hoàn toàn. Sử dụng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 51;

UPDATE `products` SET `key_ingredients` = '["Đá tro núi lửa Jeju Volcanic Clusters - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","AHA tẩy da chết - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bột vỏ quả óc chó - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bột khoáng khoáng chất - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Rửa mặt sạch và xông hơi nhẹ hoặc đắp khăn ấm để lỗ chân lông thông thoáng.
Bước 2: Thoa một lớp mặt nạ đều khắp mặt, tránh vùng mắt và môi.
Bước 3: Thư giãn trong 10-15 phút đến khi mặt nạ se khô nhẹ (không để khô nứt hẳn trên da).
Bước 4: Rửa sạch lại bằng nước ấm, vỗ nhẹ nước mát và tiếp tục các bước dưỡng da tiếp theo. Dùng 1-2 lần/tuần.' WHERE `id_product` = 52;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất nha đam tươi Jeju ép lạnh - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glycerin - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h","Centella Asiatica - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm sạch da mặt với sữa rửa mặt và cân bằng da bằng toner.
Bước 2: Lấy mặt nạ ra khỏi bao bì và đắp ôm khít toàn bộ khuôn mặt.
Bước 3: Thư giãn trong 15-20 phút để dưỡng chất thẩm thấu sâu vào da.
Bước 4: Gỡ mặt nạ ra, dùng đầu ngón tay vỗ nhẹ cho tinh chất thẩm thấu hết (không cần rửa lại với nước). Dùng 2-3 lần/tuần.' WHERE `id_product` = 53;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất mầm trà xanh Beauty Green Tea Jeju - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Axit amin tự nhiên - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Betaine - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm sạch da mặt với sữa rửa mặt và cân bằng da bằng toner.
Bước 2: Lấy mặt nạ ra khỏi bao bì và đắp ôm khít toàn bộ khuôn mặt.
Bước 3: Thư giãn trong 15-20 phút để dưỡng chất thẩm thấu sâu vào da.
Bước 4: Gỡ mặt nạ ra, dùng đầu ngón tay vỗ nhẹ cho tinh chất thẩm thấu hết (không cần rửa lại với nước). Dùng 2-3 lần/tuần.' WHERE `id_product` = 54;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất rau diếp cá (Heartleaf) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Panthenol - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Allantoin - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm sạch da mặt với sữa rửa mặt và cân bằng da bằng toner.
Bước 2: Lấy mặt nạ ra khỏi bao bì và đắp ôm khít toàn bộ khuôn mặt.
Bước 3: Thư giãn trong 15-20 phút để dưỡng chất thẩm thấu sâu vào da.
Bước 4: Gỡ mặt nạ ra, dùng đầu ngón tay vỗ nhẹ cho tinh chất thẩm thấu hết (không cần rửa lại với nước). Dùng 2-3 lần/tuần.' WHERE `id_product` = 55;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất rau má Centella Asiatica - Kích thích tăng sinh collagen, làm dịu da và phục hồi sau mụn","Madecassoside - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng"]', `usage_instruction` = 'Bước 1: Làm sạch da mặt với sữa rửa mặt và cân bằng da bằng toner.
Bước 2: Lấy mặt nạ ra khỏi bao bì và đắp ôm khít toàn bộ khuôn mặt.
Bước 3: Thư giãn trong 15-20 phút để dưỡng chất thẩm thấu sâu vào da.
Bước 4: Gỡ mặt nạ ra, dùng đầu ngón tay vỗ nhẹ cho tinh chất thẩm thấu hết (không cần rửa lại với nước). Dùng 2-3 lần/tuần.' WHERE `id_product` = 56;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất hạt yến mạch cô đặc - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Ceramide thực vật - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bơ hạt mỡ - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ"]', `usage_instruction` = 'Bước 1: Làm sạch da mặt với sữa rửa mặt và cân bằng da bằng toner.
Bước 2: Lấy mặt nạ ra khỏi bao bì và đắp ôm khít toàn bộ khuôn mặt.
Bước 3: Thư giãn trong 15-20 phút để dưỡng chất thẩm thấu sâu vào da.
Bước 4: Gỡ mặt nạ ra, dùng đầu ngón tay vỗ nhẹ cho tinh chất thẩm thấu hết (không cần rửa lại với nước). Dùng 2-3 lần/tuần.' WHERE `id_product` = 57;

UPDATE `products` SET `key_ingredients` = '["Nước ép lựu đỏ tươi nguyên chất - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dầu hạt lựu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da"]', `usage_instruction` = 'Bước 1: Làm sạch da mặt với sữa rửa mặt và cân bằng da bằng toner.
Bước 2: Lấy mặt nạ ra khỏi bao bì và đắp ôm khít toàn bộ khuôn mặt.
Bước 3: Thư giãn trong 15-20 phút để dưỡng chất thẩm thấu sâu vào da.
Bước 4: Gỡ mặt nạ ra, dùng đầu ngón tay vỗ nhẹ cho tinh chất thẩm thấu hết (không cần rửa lại với nước). Dùng 2-3 lần/tuần.' WHERE `id_product` = 58;

UPDATE `products` SET `key_ingredients` = '["Bột khoáng tự nhiên đảo Jeju - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất lá bạc hà mát lạnh - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bột Silica vi hạt - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 59;

UPDATE `products` SET `key_ingredients` = '["Beauty Green Tea Jeju giàu axit amin - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","loại Hyaluronic Acid viên nang siêu nhỏ - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Panthenol - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 60;

UPDATE `products` SET `key_ingredients` = '["Mật ong hoa cải Jeju Canola Honey - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dầu hạt cải hữu cơ - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Sáp ong tự nhiên - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bơ xoài - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 61;

UPDATE `products` SET `key_ingredients` = '["Nước trà xanh Beauty Green Tea - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Phức hợp Amino Acid Complex - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất rễ trà xanh - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 62;

UPDATE `products` SET `key_ingredients` = '["Tro núi lửa Jeju Volcanic Clusters - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","BHA (Salicylic Acid) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Nước khoáng núi lửa - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 63;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất vỏ quýt Hallabong Jeju - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dẫn xuất Vitamin C, B3, B5 - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 64;

UPDATE `products` SET `key_ingredients` = '["Trà xanh Beauty Green Tea 16 loại acid amin - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Betaine - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glycerin thực vật - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Rửa mặt thật sạch và dùng khăn bông mềm thấm ráo nước.
Bước 2: Đổ 3-4 giọt toner ra lòng bàn tay hoặc thấm ướt bông tẩy trang mỏng.
Bước 3: Vỗ nhẹ nhàng đều khắp mặt và cổ để cân bằng độ pH và mở đường cho các bước dưỡng chất tiếp theo.
Bước 4: Tiếp tục chu trình dưỡng da với serum và kem dưỡng ẩm ngay khi da còn hơi ẩm.' WHERE `id_product` = 65;

UPDATE `products` SET `key_ingredients` = '["Dẫn xuất Vitamin C ổn định (EAE) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Melasolv (hoạt chất độc quyền làm sáng) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Ceramide bảo vệ da - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Hoàn thành các bước dưỡng ẩm cơ bản vào buổi sáng.
Bước 2: Lắc đều sản phẩm (đối với dạng sữa). Lấy một lượng vừa đủ (khoảng 2 đốt ngón tay hoặc cỡ đồng xu).
Bước 3: Chấm đều lên các vùng trán, má, cằm, cổ và vỗ nhẹ đều khắp mặt cho kem tiệp vào da (không chà xát mạnh).
Bước 4: Thoa trước khi ra nắng ít nhất 15-20 phút. Thoa lại sau mỗi 2-3 giờ nếu tiếp xúc lâu với ánh nắng hoặc ra nhiều mồ hôi.' WHERE `id_product` = 66;

UPDATE `products` SET `key_ingredients` = '[".5% Vitamin C Derivative (EAE) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Melasolv độc quyền Amorepacific - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vitamin E tinh khiết - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 67;

UPDATE `products` SET `key_ingredients` = '["Blue Hyaluronic Acid siêu phân tử nano - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Caffeine kích thích tuần hoàn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glutathione chống oxy hóa - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 68;

UPDATE `products` SET `key_ingredients` = '["X Retinol thế hệ mới - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Phức hợp Peptide 21.5% - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Gừng đỏ Bio-Flavonoid - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 69;

UPDATE `products` SET `key_ingredients` = '["Bột màu khoáng xanh bạc hà - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Tinh chất lựu đỏ chống oxy hóa - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Acai Berry dưỡng sáng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 70;

UPDATE `products` SET `key_ingredients` = '["Sleeping Micro Biome™ (23.8 tỷ men vi sinh) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Squalane thực vật - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Trehalose - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Sau khi gội đầu, vuốt nhẹ cho tóc ráo bớt nước.
Bước 2: Lấy lượng kem xả/kem ủ vừa đủ thoa đều từ thân tóc đến ngọn tóc (cách chân tóc 3-5cm để tránh bết da đầu).
Bước 3: Mát-xa nhẹ nhàng các lọn tóc và ủ trong 2-5 phút (hoặc 10-15 phút đối với kem ủ sâu).
Bước 4: Xả sạch kỹ lại bằng nước mát để khép biểu bì tóc, giúp tóc bóng mượt bồng bềnh.' WHERE `id_product` = 71;

UPDATE `products` SET `key_ingredients` = '["Peony & Collagen Complex™ - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","0.000 hạt Hydro-Melt Glow Capsules - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Peptide đàn hồi - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Sau khi gội đầu, vuốt nhẹ cho tóc ráo bớt nước.
Bước 2: Lấy lượng kem xả/kem ủ vừa đủ thoa đều từ thân tóc đến ngọn tóc (cách chân tóc 3-5cm để tránh bết da đầu).
Bước 3: Mát-xa nhẹ nhàng các lọn tóc và ủ trong 2-5 phút (hoặc 10-15 phút đối với kem ủ sâu).
Bước 4: Xả sạch kỹ lại bằng nước mát để khép biểu bì tóc, giúp tóc bóng mượt bồng bềnh.' WHERE `id_product` = 72;

UPDATE `products` SET `key_ingredients` = '["Blue Hyaluronic Acid nano - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Hạt siêu nhỏ Cellulose tan chảy - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Enzyme đu đủ Papain - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 73;

UPDATE `products` SET `key_ingredients` = '["Blue Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Chiết xuất nấm men Beta-Glucan - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Panthenol - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương"]', `usage_instruction` = 'Bước 1: Rửa mặt thật sạch và dùng khăn bông mềm thấm ráo nước.
Bước 2: Đổ 3-4 giọt toner ra lòng bàn tay hoặc thấm ướt bông tẩy trang mỏng.
Bước 3: Vỗ nhẹ nhàng đều khắp mặt và cổ để cân bằng độ pH và mở đường cho các bước dưỡng chất tiếp theo.
Bước 4: Tiếp tục chu trình dưỡng da với serum và kem dưỡng ẩm ngay khi da còn hơi ẩm.' WHERE `id_product` = 74;

UPDATE `products` SET `key_ingredients` = '["% Panthenol (Vitamin B5) - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Madecassoside (tinh chất rau má) - Kích thích tăng sinh collagen, làm dịu da và phục hồi sau mụn","Hệ vi sinh Tribioma độc quyền - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Kẽm & Mangan kháng khuẩn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 75;

UPDATE `products` SET `key_ingredients` = '["Màng lọc Mexoryl 400 độc quyền - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hoạt chất kiềm dầu Airlicium - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Nước khoáng La Roche-Posay - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Hoàn thành các bước dưỡng ẩm cơ bản vào buổi sáng.
Bước 2: Lắc đều sản phẩm (đối với dạng sữa). Lấy một lượng vừa đủ (khoảng 2 đốt ngón tay hoặc cỡ đồng xu).
Bước 3: Chấm đều lên các vùng trán, má, cằm, cổ và vỗ nhẹ đều khắp mặt cho kem tiệp vào da (không chà xát mạnh).
Bước 4: Thoa trước khi ra nắng ít nhất 15-20 phút. Thoa lại sau mỗi 2-3 giờ nếu tiếp xúc lâu với ánh nắng hoặc ra nhiều mồ hôi.' WHERE `id_product` = 76;

UPDATE `products` SET `key_ingredients` = '["Phylobioma độc quyền - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Procerad ngừa thâm - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Niacinamide - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","LHA & Salicylic Acid - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 77;

UPDATE `products` SET `key_ingredients` = '["Công nghệ Glyco Micellar - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Kẽm PCA (Zinc PCA) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Nước khoáng La Roche-Posay - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Thấm một lượng nước tẩy trang vừa đủ ướt đẫm miếng bông cotton chuyên dụng.
Bước 2: Áp miếng bông lên mắt và môi trong 5-10 giây để hòa tan lớp trang điểm cứng đầu, sau đó vuốt nhẹ nhàng.
Bước 3: Dùng miếng bông mới lau đều khắp mặt từ trong ra ngoài và từ dưới lên trên đến khi bông sạch hoàn toàn.
Bước 4: Rửa sạch lại với nước và sữa rửa mặt (Double Cleansing) để lỗ chân lông thông thoáng hoàn toàn.' WHERE `id_product` = 78;

UPDATE `products` SET `key_ingredients` = '["Hyaluronic Acid 2 kích thước phân tử - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","% Vitamin B5 (Panthenol) - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Madecassoside - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 79;

UPDATE `products` SET `key_ingredients` = '["Kẽm PCA (Zinc PCA) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Nước khoáng nhiệt La Roche-Posay - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Gốc làm sạch dịu nhẹ không xà phòng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 80;

UPDATE `products` SET `key_ingredients` = '["Niacinamide tinh khiết - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","Glycerin cấp ẩm - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h","Nước khoáng làm dịu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 81;

UPDATE `products` SET `key_ingredients` = '["% Salicylic Acid (BHA) - Thẩm thấu sâu vào lỗ chân lông, loại bỏ bã nhờn và tế bào chết tích tụ","LHA (Lipo-Hydroxy Acid) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Kẽm PCA - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Làm ướt toàn bộ da mặt bằng nước ấm để giãn nở lỗ chân lông.
Bước 2: Lấy một lượng gel/sữa khoảng 1-2 hạt đậu ra lòng bàn tay ướt và tạo bọt mịn.
Bước 3: Mát-xa nhẹ nhàng theo chiều xoắn ốc từ dưới lên trên trong 60 giây, tập trung kỹ vùng chữ T.
Bước 4: Rửa sạch lại bằng nước mát để se khít lỗ chân lông và thấm khô bằng khăn mềm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 82;

UPDATE `products` SET `key_ingredients` = '["Zinc Oxide & Titanium Dioxide - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bio-Active Antioxidant Network - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bio-Sugar Complex cấp ẩm - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Hoàn thành các bước dưỡng ẩm cơ bản vào buổi sáng.
Bước 2: Lắc đều sản phẩm (đối với dạng sữa). Lấy một lượng vừa đủ (khoảng 2 đốt ngón tay hoặc cỡ đồng xu).
Bước 3: Chấm đều lên các vùng trán, má, cằm, cổ và vỗ nhẹ đều khắp mặt cho kem tiệp vào da (không chà xát mạnh).
Bước 4: Thoa trước khi ra nắng ít nhất 15-20 phút. Thoa lại sau mỗi 2-3 giờ nếu tiếp xúc lâu với ánh nắng hoặc ra nhiều mồ hôi.' WHERE `id_product` = 83;

UPDATE `products` SET `key_ingredients` = '["1 loại Axit Amin - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Ceramides & Phospholipids - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Glycerin & Urea - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 84;

UPDATE `products` SET `key_ingredients` = '["3% L-Ascorbic Acid nguyên chất - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","% Hyaluronic Acid Spheres dạng viên nang - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 85;

UPDATE `products` SET `key_ingredients` = '["% Salicylic Acid (BHA) - Thẩm thấu sâu vào lỗ chân lông, loại bỏ bã nhờn và tế bào chết tích tụ","Than hoạt tính (Charcoal Powder) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Đất sét Amazon (Kaolin & Squalane) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Rửa mặt sạch và xông hơi nhẹ hoặc đắp khăn ấm để lỗ chân lông thông thoáng.
Bước 2: Thoa một lớp mặt nạ đều khắp mặt, tránh vùng mắt và môi.
Bước 3: Thư giãn trong 10-15 phút đến khi mặt nạ se khô nhẹ (không để khô nứt hẳn trên da).
Bước 4: Rửa sạch lại bằng nước ấm, vỗ nhẹ nước mát và tiếp tục các bước dưỡng da tiếp theo. Dùng 1-2 lần/tuần.' WHERE `id_product` = 86;

UPDATE `products` SET `key_ingredients` = '["% Salicylic Acid (BHA) - Thẩm thấu sâu vào lỗ chân lông, loại bỏ bã nhờn và tế bào chết tích tụ","Squalane tự nhiên làm dịu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 87;

UPDATE `products` SET `key_ingredients` = '["% Hyaluronic Acid (đa phân tử) - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Vitamin B5 (Panthenol) - Làm dịu tức thì tình trạng ngứa rát, mẩn đỏ và phục hồi tế bào da tổn thương","Ahnfeltia Concinna Extract - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 88;

UPDATE `products` SET `key_ingredients` = '["Matrixyl 3000 & Matrixyl Synthe 6 - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Syn-Ake peptide - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Argirelox peptide - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Phức hợp Probiotic và HA - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 89;

UPDATE `products` SET `key_ingredients` = '["0% Niacinamide (Vitamin B3) - Phục hồi hàng rào ẩm, điều tiết bã nhờn và dưỡng sáng đều màu da","% Kẽm PCA (Zinc PCA) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất hạt me - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 90;

UPDATE `products` SET `key_ingredients` = '["0% AHA tổng hợp - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","% Salicylic Acid (BHA) - Thẩm thấu sâu vào lỗ chân lông, loại bỏ bã nhờn và tế bào chết tích tụ","Chiết xuất tiêu Tasmanian làm dịu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hyaluronic Acid & Vitamin B5 - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng"]', `usage_instruction` = 'Bước 1: Làm sạch vùng da cần chăm sóc với nước ấm để làm mềm lớp sừng già cỗi.
Bước 2: Lấy một lượng sản phẩm vừa đủ thoa đều lên vùng da còn ẩm.
Bước 3: Mát-xa nhẹ nhàng theo chuyển động tròn trong 2-3 phút, tập trung vùng da sần sùi (khuỷu tay, đầu gối, cánh mũi).
Bước 4: Rửa sạch lại với nước và thoa ngay kem dưỡng ẩm/dưỡng thể. Dùng đều đặn 1-2 lần/tuần, không lạm dụng.' WHERE `id_product` = 91;

UPDATE `products` SET `key_ingredients` = '["% Alpha Arbutin tinh khiết - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hyaluronic Acid phân tử đa tầng - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng","Axit lactic - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 92;

UPDATE `products` SET `key_ingredients` = '["% Caffeine đậm đặc - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","EGCG chiết xuất từ trà xanh tinh khiết - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hyaluronic Acid - Phân tử ngậm nước gấp 1000 lần trọng lượng, giữ da luôn căng mọng"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 93;

UPDATE `products` SET `key_ingredients` = '["% Glycolic Acid (AHA) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Nước cất hoa hồng & hoa cúc - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất tiêu Tasmanian làm dịu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Nhân sâm dưỡng da - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Rửa mặt thật sạch và dùng khăn bông mềm thấm ráo nước.
Bước 2: Đổ 3-4 giọt toner ra lòng bàn tay hoặc thấm ướt bông tẩy trang mỏng.
Bước 3: Vỗ nhẹ nhàng đều khắp mặt và cổ để cân bằng độ pH và mở đường cho các bước dưỡng chất tiếp theo.
Bước 4: Tiếp tục chu trình dưỡng da với serum và kem dưỡng ẩm ngay khi da còn hơi ẩm.' WHERE `id_product` = 94;

UPDATE `products` SET `key_ingredients` = '["Bột khoáng Talc siêu mịn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Mica phát sáng tự nhiên - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Silica kiềm dầu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 95;

UPDATE `products` SET `key_ingredients` = '["Bột phấn khoáng thiên nhiên - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dầu dừa làm mềm hạt phấn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chất tạo màu an toàn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 96;

UPDATE `products` SET `key_ingredients` = '["Dimethicone làm mịn vân môi - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Nước khoáng cấp ẩm - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chất gel mờ kháng lem - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 97;

UPDATE `products` SET `key_ingredients` = '["Gel cấp ẩm Water-base - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Polyme khóa màu chống trôi - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Sắc tố khoáng đậm đặc - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 98;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất dầu hướng dương - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dầu hoa cúc La Mã dưỡng ẩm - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bột nhung tạo độ lì mịn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 99;

UPDATE `products` SET `key_ingredients` = '["Hương đầu: Cam Bergamot, Tiêu hồng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hương giữa: Hoa hồng Thổ Nhĩ Kỳ, Hoa nhài - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hương cuối: Hoắc hương, Hổ phách, Vanilla Bourbon - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 100;

UPDATE `products` SET `key_ingredients` = '["Hương đầu: Quả chanh vàng, Tuyết tùng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hương giữa: Hoa nhài, Lục bình, Tiêu hồng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hương cuối: Gỗ tếch, Xạ hương trắng, Hổ phách - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 101;

UPDATE `products` SET `key_ingredients` = '["Hương đầu: Quả bưởi, Cam Bergamot - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hương giữa: Hoa hồng, Hoa thủy tiên, Hoa phong lữ - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hương cuối: Hoắc hương Indonesia, Đậu Tonka, Gỗ đàn hương - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 102;

UPDATE `products` SET `key_ingredients` = '["Aldehyde phát sáng - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hoa ngọc lan tây Ylang-Ylang - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hoa hồng tháng Năm May Rose xứ Grasse - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Gỗ đàn hương & Vani - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 103;

UPDATE `products` SET `key_ingredients` = '["Hợp chất Aldehydes độc bản - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Tinh dầu hoa nhài Pháp - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hoa huệ tây - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hổ phách & Cỏ vetiver - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 104;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất tinh dầu nước hoa Chanel nguyên chất - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Glycerin thực vật - Hoạt chất cấp ẩm sâu đa tầng, duy trì màng ẩm tự nhiên suốt 48h","Chất làm sạch dịu nhẹ - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 105;

UPDATE `products` SET `key_ingredients` = '["Bột ngọc trai siêu mịn Pinklite - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất trà xanh chống oxy hóa - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vitamin E & Bơ hạt mỡ - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do"]', `usage_instruction` = 'Bước 1: Vệ sinh da mặt sạch sẽ và cân bằng da với toner/serum.
Bước 2: Lấy một lượng kem dưỡng vừa đủ (cỡ hạt đậu) chấm đều lên 5 điểm: trán, mũi, hai má và cằm.
Bước 3: Thoa đều và mát-xa nhẹ nhàng theo chiều nâng cơ để kem thẩm thấu sâu vào da mà không làm chảy xệ.
Bước 4: Áp nhẹ hai lòng bàn tay ấm lên mặt trong 10-15 giây để tăng hiệu quả khóa ẩm. Dùng đều đặn 2 lần sáng và tối.' WHERE `id_product` = 111;

UPDATE `products` SET `key_ingredients` = '["Phức hợp Lightful C3 (Vitamin C, San hô, Hoa anh đào) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bột màu Lavender hiệu chỉnh quang học - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 112;

UPDATE `products` SET `key_ingredients` = '["Hạt bột màu phủ dưỡng chất - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Sáp vi tinh thể - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chất làm mềm nhung mịn - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 113;

UPDATE `products` SET `key_ingredients` = '["Bơ hạt mỡ hữu cơ - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ","Bơ ca cao - Khóa ẩm sâu, ngăn ngừa thoát nước và phục hồi da khô ráp","Dầu dừa tinh khiết - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 114;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất bơ hạt mỡ - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ","Dầu dừa organic - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Sáp Candelilla - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 115;

UPDATE `products` SET `key_ingredients` = '["Bột đất sét khoáng hút dầu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Hạt phấn siêu vi Micro-powders - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 116;

UPDATE `products` SET `key_ingredients` = '["Hạt màu bọc polyme kháng nước - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bột khoáng kiềm dầu - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Vệ sinh sạch vùng da cần chăm sóc trước khi sử dụng sản phẩm.
Bước 2: Lấy một lượng sản phẩm vừa đủ ra đầu ngón tay hoặc lòng bàn tay.
Bước 3: Thoa đều nhẹ nhàng lên da và mát-xa theo chiều nâng cơ trong 1-2 phút để dưỡng chất thẩm thấu tối đa.
Bước 4: Sử dụng đều đặn mỗi ngày (sáng và tối) để đạt hiệu quả cải thiện da tốt nhất. Đậy kín nắp sau khi dùng.' WHERE `id_product` = 117;

UPDATE `products` SET `key_ingredients` = '["Vitamin E dưỡng ẩm - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do","Hạt phấn nhung siêu mịn Dimensional Matte - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 118;

UPDATE `products` SET `key_ingredients` = '["Công nghệ khóa màu Color Lock - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất nha đam cấp ẩm - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vitamin E - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 119;

UPDATE `products` SET `key_ingredients` = '["Bơ hạt mỡ dưỡng ẩm - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ","Sáp ong tự nhiên - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Sắc tố màu đậm đặc - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 120;

UPDATE `products` SET `key_ingredients` = '["Dầu nụ tầm xuân (Rosehip Oil) - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dầu Argan - Cung cấp acid béo và vitamin E nuôi dưỡng sợi tóc suôn mượt vào nếp","Dầu hoa anh thảo - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bơ hạt mỡ - Khóa ẩm chuyên sâu, làm mềm mịn và làm dịu các vùng da nứt nẻ"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 121;

UPDATE `products` SET `key_ingredients` = '["Phức hợp tinh dầu quả mơ - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dầu hạt jojoba - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Nước khoáng tinh khiết - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 122;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất quả kiwi - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chiết xuất đu đủ & bưởi - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Chất bóng ngậm nước - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 123;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất hạt macadamia - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Tinh dầu dừa cấp ẩm - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Vitamin E - Chất chống oxy hóa mạnh mẽ, bảo vệ màng tế bào da khỏi gốc tự do"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 124;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất quả lê tự nhiên - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Dưỡng chất cấp ẩm trái cây - Hoạt chất lành tính đạt chuẩn da liễu quốc tế","Bột màu tint siêu bền - Hoạt chất lành tính đạt chuẩn da liễu quốc tế"]', `usage_instruction` = 'Bước 1: Tẩy tế bào chết môi định kỳ và thoa một lớp son dưỡng mỏng trước khi đánh son 5 phút.
Bước 2: Dùng khăn giấy bặm nhẹ để lấy đi lượng dầu thừa của son dưỡng.
Bước 3: Dùng đầu cọ lấy lượng son vừa đủ, chấm vào lòng môi dưới rồi tán nhẹ ra viền môi để tạo hiệu ứng ombre tự nhiên.
Bước 4: Chờ 1-2 phút để lớp son set chặt trên môi trước khi ăn uống hoặc đeo khẩu trang.' WHERE `id_product` = 125;

UPDATE `products` SET `key_ingredients` = '["Chiết xuất trà xanh, nha đam, rau má, lựu đỏ, diếp cá, yến mạch Jeju - Kích thích tăng sinh collagen, làm dịu da và phục hồi sau mụn"]', `usage_instruction` = 'Bước 1: Làm sạch da mặt với sữa rửa mặt và cân bằng da bằng toner.
Bước 2: Lấy mặt nạ ra khỏi bao bì và đắp ôm khít toàn bộ khuôn mặt.
Bước 3: Thư giãn trong 15-20 phút để dưỡng chất thẩm thấu sâu vào da.
Bước 4: Gỡ mặt nạ ra, dùng đầu ngón tay vỗ nhẹ cho tinh chất thẩm thấu hết (không cần rửa lại với nước). Dùng 2-3 lần/tuần.' WHERE `id_product` = 126;
