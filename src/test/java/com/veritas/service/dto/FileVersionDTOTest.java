package com.veritas.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.veritas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FileVersionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FileVersionDTO.class);
        FileVersionDTO fileVersionDTO1 = new FileVersionDTO();
        fileVersionDTO1.setId(1L);
        FileVersionDTO fileVersionDTO2 = new FileVersionDTO();
        assertThat(fileVersionDTO1).isNotEqualTo(fileVersionDTO2);
        fileVersionDTO2.setId(fileVersionDTO1.getId());
        assertThat(fileVersionDTO1).isEqualTo(fileVersionDTO2);
        fileVersionDTO2.setId(2L);
        assertThat(fileVersionDTO1).isNotEqualTo(fileVersionDTO2);
        fileVersionDTO1.setId(null);
        assertThat(fileVersionDTO1).isNotEqualTo(fileVersionDTO2);
    }
}
