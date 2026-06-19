package com.app.hubble.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class HashUtilsTest {

    @Test
    void sha256HexGeneraHashDeterministico() {
        String first = HashUtils.sha256Hex("refresh-token-test");
        String second = HashUtils.sha256Hex("refresh-token-test");
        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(64);
    }

    @Test
    void sha256HexProduceValoresDistintosParaEntradasDistintas() {
        String first = HashUtils.sha256Hex("token-a");
        String second = HashUtils.sha256Hex("token-b");
        assertThat(first).isNotEqualTo(second);
    }
}
