package application;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VehicleRentalTest {

	@Test
    public void testLicensePlateValidation() {
        Vehicle car1 = new Car("Toyota", "Corolla", 2025, 4);
        Vehicle motorcycle1 = new Motorcycle("Harley", "Quinn", 2014, true);
        Vehicle truck1 = new Truck("Ford", "Monster", 2017, 34.44);

        try {
            car1.setLicensePlate("AAA100");
            assertTrue(car1.getLicensePlate().matches("[A-Za-z]{3}[0-9]{3}"));
        } catch (IllegalArgumentException e) {
            fail("Valid plate AAA100 should not throw an exception.");
        }

        try {
            motorcycle1.setLicensePlate("ZZZ999");
            assertTrue(motorcycle1.getLicensePlate().matches("[A-Za-z]{3}[0-9]{3}"));
        } catch (IllegalArgumentException e) {
            fail("Valid plate ZZZ999 should not throw an exception.");
        }

        try {
            truck1.setLicensePlate("ABC567");
            assertTrue(truck1.getLicensePlate().matches("[A-Za-z]{3}[0-9]{3}"));
        } catch (IllegalArgumentException e) {
            fail("Valid plate ABC567 should not throw an exception.");
        }
        
        Vehicle carInvalid = new Car("Ford", "Mustang", 2019, 5);
        Vehicle motorcycleInvalid = new Motorcycle("Ducati", "Davidson", 2023, false);

        assertThrows(IllegalArgumentException.class, () -> carInvalid.setLicensePlate(""));
        assertThrows(IllegalArgumentException.class, () -> motorcycleInvalid.setLicensePlate(null));
        assertThrows(IllegalArgumentException.class, () -> carInvalid.setLicensePlate("AAA1000"));
        assertThrows(IllegalArgumentException.class, () -> motorcycleInvalid.setLicensePlate("ZZZ99"));
        
        assertNull(carInvalid.getLicensePlate());
        assertNull(motorcycleInvalid.getLicensePlate());
        
	}
	//Test Case 2
	
	private RentalSystem rentalSystem;
    private Vehicle vehicle;
    private Customer customer;

    @BeforeEach
    public void setUp() {
        rentalSystem = RentalSystem.getInstance();
        vehicle = new Car("Toyota", "Corolla", 2025, 4);
        vehicle.setLicensePlate("AAA100");
 
        customer = new Customer(007, "James Bond");
    }

    @Test
    public void testRentAndReturnVehicle() {
    	assertEquals(Vehicle.VehicleStatus.AVAILABLE, vehicle.getStatus());

        boolean rentSuccess = rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), 100.0);
        assertTrue(rentSuccess, "Renting the vehicle should succeed");
        assertEquals(Vehicle.VehicleStatus.RENTED, vehicle.getStatus(), "The vehicle should be marked as RENTED after renting");

        boolean rentAgainSuccess = rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), 100.0);
        assertFalse(rentAgainSuccess, "Renting the same vehicle again should fail");

        boolean returnSuccess = rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), 50.0);
        assertTrue(returnSuccess, "Returning the vehicle should succeed");
        assertEquals(Vehicle.VehicleStatus.AVAILABLE, vehicle.getStatus(), "The vehicle should be marked as AVAILABLE after returning");

        boolean returnAgainSuccess = rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), 50.0);
        assertFalse(returnAgainSuccess, "Returning the same vehicle again should fail");
        }
    
    //test case 3
    
    @Test
    public void testSingletonRentalSystem() {
        try {
            Constructor<RentalSystem> constructor = RentalSystem.class.getDeclaredConstructor();
            int modifiers = constructor.getModifiers();
            assertTrue(Modifier.isPrivate(modifiers), "constructor should be private");
            constructor.setAccessible(true); 
            
        } catch (Exception e) {
            assertTrue(e instanceof IllegalAccessException || e instanceof InstantiationException,
                       "Expected IllegalAccessException or InstantiationException");
        }

        RentalSystem rentalSystem = RentalSystem.getInstance();
        assertNotNull(rentalSystem, "Should not be null");

        RentalSystem anotherInstance = RentalSystem.getInstance();
        assertSame(rentalSystem, anotherInstance, "Should be the same");
    }
}
