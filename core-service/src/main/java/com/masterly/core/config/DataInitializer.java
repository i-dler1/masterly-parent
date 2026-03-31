//package com.masterly.core.config;
//
//import com.masterly.core.model.Client;
//import com.masterly.core.model.Master;
//import com.masterly.core.model.ServiceEntity;
//import com.masterly.core.model.Material;
//import com.masterly.core.model.ServiceMaterial;
//import com.masterly.core.repository.ClientRepository;
//import com.masterly.core.repository.MasterRepository;
//import com.masterly.core.repository.ServiceEntityRepository;
//import com.masterly.core.repository.MaterialRepository;
//import com.masterly.core.repository.ServiceMaterialRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import java.math.BigDecimal;
//
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//
//    private final MasterRepository masterRepository;
//    private final ClientRepository clientRepository;
//    private final ServiceEntityRepository serviceRepository;
//    private final MaterialRepository materialRepository;
//    private final ServiceMaterialRepository serviceMaterialRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        // Создаем мастера если его нет
//        if (masterRepository.count() == 0) {
//            Master master = new Master();
//            master.setEmail("test@master.com");
//            master.setPasswordHash("123");
//            master.setFullName("Test Master");
//            masterRepository.save(master);
//            System.out.println("✅ Test Master created");
//        }
//
//        // Получаем мастера
//        Master master = masterRepository.findAll().get(0);
//
//        // Создаем клиента если его нет
//        if (clientRepository.count() == 0) {
//            Client client = new Client();
//            client.setMaster(master);
//            client.setFullName("Анна Смирнова");
//            client.setPhone("+7 (999) 123-45-67");
//            client.setEmail("anna@example.com");
//            clientRepository.save(client);
//            System.out.println("✅ Test Client created");
//        }
//
//        // Создаем услугу если ее нет
//        if (serviceRepository.count() == 0) {
//            ServiceEntity service = new ServiceEntity();
//            service.setMaster(master);
//            service.setName("Классическое наращивание");
//            service.setDescription("Поресничное наращивание");
//            service.setDurationMinutes(120);
//            service.setPrice(new BigDecimal("2500"));
//            service.setCategory("наращивание");
//            serviceRepository.save(service);
//            System.out.println("✅ Test Service created");
//        }
//
//        // Создаем материал если его нет
//        if (materialRepository.count() == 0) {
//            Material material = new Material();
//            material.setMaster(master);
//            material.setName("Клей для ресниц");
//            material.setUnit("мл");
//            material.setQuantity(new BigDecimal("10"));
//            material.setMinQuantity(new BigDecimal("2"));
//            material.setPricePerUnit(new BigDecimal("1500"));
//            material.setSupplier("LashPro");
//            materialRepository.save(material);
//            System.out.println("✅ Test Material created");
//        }
//
//        // Создаем связь услуги с материалом если ее нет
//        if (serviceMaterialRepository.count() == 0) {
//            ServiceEntity service = serviceRepository.findAll().get(0);
//            Material material = materialRepository.findAll().get(0);
//
//            ServiceMaterial serviceMaterial = new ServiceMaterial();
//            serviceMaterial.setService(service);
//            serviceMaterial.setMaterial(material);
//            serviceMaterial.setQuantityUsed(new BigDecimal("2"));
//            serviceMaterialRepository.save(serviceMaterial);
//            System.out.println("✅ Service-Material link created");
//        }
//
//        System.out.println("=== Test data initialized ===");
//    }
//}