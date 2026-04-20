package com.masterly.core.repository;

import com.masterly.core.config.BaseTestcontainersTest;
import com.masterly.core.config.TestcontainersSingleton;
import com.masterly.core.entity.Client;
import com.masterly.core.entity.Master;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=true",
        "spring.jpa.hibernate.ddl-auto=none"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ClientRepositoryTest extends BaseTestcontainersTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private MasterRepository masterRepository;

    private Master master;
    private Client client;

    @BeforeEach
    void setUp() {
        // Очищаем БД перед каждым тестом
        clientRepository.deleteAll();
        masterRepository.deleteAll();

        // Уникальный email
        String uniqueEmail = "test-" + System.currentTimeMillis() + "@master.com";

        master = new Master();
        master.setEmail(uniqueEmail);  // ✅ Уникальный email
        master.setPasswordHash("password");
        master.setFullName("Test Master");
        master.setRole("MASTER");
        master.setIsActive(true);
        master = masterRepository.save(master);

        client = new Client();
        client.setFullName("Test Client");
        client.setPhone("+375291234567");
        client.setEmail("client@test.com");
        client.setBirthDate(LocalDate.of(1990, 1, 1));
        client.setMaster(master);
        client.setRole("CLIENT");
        client.setIsRegular(false);
        client = clientRepository.save(client);
    }

    @BeforeAll
    static void setupDatabase() {
        TestcontainersSingleton.clean();
    }

    @Test
    void testFindByMasterId_ShouldReturnClients() {
        List<Client> clients = clientRepository.findByMasterId(master.getId());
        assertThat(clients).hasSize(1);
        assertThat(clients.get(0).getFullName()).isEqualTo("Test Client");
    }

    @Test
    void testBirthDatePersistence() {
        Client saved = clientRepository.findById(client.getId()).orElseThrow();
        assertThat(saved.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
    }
}