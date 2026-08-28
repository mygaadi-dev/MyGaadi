package com.mygaadi.config;

import com.mygaadi.model.entity.Car;
import com.mygaadi.model.entity.CarImage;
import com.mygaadi.model.entity.User;
import com.mygaadi.model.enums.CarStatus;
import com.mygaadi.model.enums.FuelType;
import com.mygaadi.model.enums.Role;
import com.mygaadi.model.enums.Transmission;
import com.mygaadi.repository.CarRepository;
import com.mygaadi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    @Value("${app.demo-data.enabled:true}")
    private boolean demoDataEnabled;

    @Value("${app.demo-data.base-url:http://localhost:8080}")
    private String demoBaseUrl;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!demoDataEnabled) return;

        userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.SELLER && !user.isDeleted())
                .findFirst()
                .ifPresent(seller -> demoCars().forEach(spec -> upsertCar(seller, spec)));
    }

    private void upsertCar(User seller, DemoCar spec) {
        Car car = carRepository.findFirstByTitleIgnoreCaseAndSeller(spec.title(), seller)
                .or(() -> spec.legacyTitle() == null ? java.util.Optional.empty()
                        : carRepository.findFirstByTitleIgnoreCaseAndSeller(spec.legacyTitle(), seller))
                .orElseGet(() -> Car.builder().seller(seller).title(spec.title()).build());

        car.setSeller(seller);
        car.setTitle(spec.title());
        car.setBrand(spec.brand());
        car.setModel(spec.model());
        car.setVariant(spec.variant());
        car.setYear(spec.year());
        car.setPrice(spec.price());
        BigDecimal seedBookingAmount = spec.price().multiply(new BigDecimal("0.02")).setScale(2, java.math.RoundingMode.HALF_UP);
        if (seedBookingAmount.compareTo(new BigDecimal("70000.00")) >= 0) {
            seedBookingAmount = new BigDecimal("50000.00");
        }
        if (seedBookingAmount.compareTo(new BigDecimal("5000.00")) < 0) {
            seedBookingAmount = new BigDecimal("5000.00");
        }
        car.setBookingAmount(seedBookingAmount);
        car.setFuelType(spec.fuelType());
        car.setTransmission(spec.transmission());
        car.setMileageKm(spec.mileageKm());
        car.setEngineCc(spec.engineCc());
        car.setColor(spec.color());
        car.setNoOfOwners(spec.owners());
        car.setInsuranceValidTill(LocalDate.now().plusMonths(10));
        car.setRcAvailable(true);
        car.setLocationCity(spec.city());
        car.setLocationState("Maharashtra");
        car.setDescription(spec.description());
        car.setStatus(CarStatus.ACTIVE);
        car.setFeatured(spec.featured());
        car.setDeleted(false);
        car.setDeletedAt(null);

        car.getImages().clear();
        addDemoImage(car, spec.slug(), "front", "FRONT", 0, true);
        addDemoImage(car, spec.slug(), "side", "SIDE", 1, false);
        addDemoImage(car, spec.slug(), "rear", "REAR", 2, false);

        carRepository.save(car);
    }

    private void addDemoImage(Car car, String slug, String file, String type, int order, boolean primary) {
        String url = demoBaseUrl + "/demo-cars/" + slug + "/" + file + ".jpg";
        car.addImage(CarImage.builder()
                .imageUrl(url)
                .thumbnailUrl(url)
                .imageType(type)
                .displayOrder(order)
                .primaryImage(primary)
                .deleted(false)
                .build());
    }

    private List<DemoCar> demoCars() {
        return List.of(
                new DemoCar("Maruti Suzuki Swift VXI 2020", "Maruti Suzuki", "Swift", "VXI", 2020,
                        "545000.00", FuelType.PETROL, Transmission.MANUAL, 42000, 1197, "Red", 1,
                        "Pune", true, "Single-owner Swift with a smooth petrol engine, clean cabin and complete service history.", "swift-2020", null),
                new DemoCar("Hyundai Creta SX Diesel 2021", "Hyundai", "Creta", "SX", 2021,
                        "1285000.00", FuelType.DIESEL, Transmission.MANUAL, 51000, 1493, "Black", 1,
                        "Mumbai", true, "Well-maintained Creta with premium features, strong diesel performance and excellent highway comfort.", "creta-2021", "Hyundai Creta SX Diesel"),
                new DemoCar("Honda City VX CVT 2019", "Honda", "City", "VX CVT", 2019,
                        "875000.00", FuelType.PETROL, Transmission.AUTOMATIC, 39000, 1497, "Silver", 1,
                        "Nashik", false, "Refined automatic sedan with a spacious rear seat, large boot and documented maintenance.", "city-2019", null),
                new DemoCar("Tata Nexon XZ Plus 2022", "Tata", "Nexon", "XZ Plus", 2022,
                        "995000.00", FuelType.PETROL, Transmission.MANUAL, 26000, 1199, "Blue", 1,
                        "Nagpur", true, "Low-kilometre compact SUV with high ground clearance, modern infotainment and strong safety package.", "nexon-2022", null),
                new DemoCar("Mahindra XUV700 AX5 2022", "Mahindra", "XUV700", "AX5", 2022,
                        "1845000.00", FuelType.DIESEL, Transmission.AUTOMATIC, 31000, 2184, "Red", 1,
                        "Pune", true, "Powerful automatic SUV with premium cabin, connected features and confident road presence.", "xuv700-2022", null),
                new DemoCar("Toyota Innova Crysta GX 2018", "Toyota", "Innova Crysta", "GX", 2018,
                        "1575000.00", FuelType.DIESEL, Transmission.MANUAL, 76000, 2393, "Grey", 2,
                        "Kolhapur", false, "Reliable seven-seat family MPV with a durable diesel engine and well-kept interior.", "innova-2018", null),
                new DemoCar("Kia Seltos HTX 2021", "Kia", "Seltos", "HTX", 2021,
                        "1375000.00", FuelType.PETROL, Transmission.AUTOMATIC, 44000, 1497, "White", 1,
                        "Thane", true, "Stylish automatic SUV with premium upholstery, touchscreen infotainment and complete paperwork.", "seltos-2021", null),
                new DemoCar("Maruti Suzuki Baleno Zeta 2020", "Maruti Suzuki", "Baleno", "Zeta", 2020,
                        "635000.00", FuelType.PETROL, Transmission.MANUAL, 36000, 1197, "Blue", 1,
                        "Pune", false, "Fuel-efficient premium hatchback with low running costs and a clean, practical cabin.", "baleno-2020", "Maruti Suzuki Baleno Alpha"),
                new DemoCar("MG Hector Sharp DCT 2021", "MG", "Hector", "Sharp DCT", 2021,
                        "1490000.00", FuelType.PETROL, Transmission.DCT, 41000, 1451, "Black", 1,
                        "Mumbai", false, "Feature-rich SUV with panoramic sunroof, connected-car technology and roomy seats.", "hector-2021", null),
                new DemoCar("Tata Tiago XZ CNG 2022", "Tata", "Tiago", "XZ CNG", 2022,
                        "595000.00", FuelType.CNG, Transmission.MANUAL, 28000, 1199, "Orange", 1,
                        "Aurangabad", false, "Economical CNG hatchback ideal for daily commuting, with low mileage and clean condition.", "tiago-2022", null)
        );
    }

    private record DemoCar(
            String title,
            String brand,
            String model,
            String variant,
            int year,
            BigDecimal price,
            FuelType fuelType,
            Transmission transmission,
            int mileageKm,
            int engineCc,
            String color,
            int owners,
            String city,
            boolean featured,
            String description,
            String slug,
            String legacyTitle
    ) {
        private DemoCar(String title, String brand, String model, String variant, int year,
                        String price, FuelType fuelType, Transmission transmission, int mileageKm,
                        int engineCc, String color, int owners, String city, boolean featured,
                        String description, String slug, String legacyTitle) {
            this(title, brand, model, variant, year, new BigDecimal(price), fuelType, transmission,
                    mileageKm, engineCc, color, owners, city, featured, description, slug, legacyTitle);
        }
    }
}
