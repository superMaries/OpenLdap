package cn.ldap.ldap.common.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 恢复的DTO
 *
 * @title: ImportDto
 * @Author Wy
 * @Date: 2023/4/18 9:22
 * @Version 1.0
 */
@Data
public class ImportDto {
    /**
     * 上传的文件
     */
    private MultipartFile file;
    /**
     * 恢复的模式
     * 1 仅添加  2 仅更新 3 更新或添加
     */
    private Integer type;
}
