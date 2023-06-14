package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

public class FareCalculatorServiceTest {

    private FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeEach
    private void setUp() {
        fareCalculatorService = new FareCalculatorService();
        ticket = new Ticket();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType(){
        Ticket ticket = new Ticket();
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Ticket ticket = new Ticket();
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR),ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test 
    public void calculateFareCarWithLessThan30MinutesParkingTime() { 
     
        Date inTime = new Date(); 
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000));  
        Date outTime = new Date(); 
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime); 
        ticket.setOutTime(outTime); 
        ticket.setParkingSpot(parkingSpot); 
        fareCalculatorService.calculateFare(ticket); 
        assertEquals(0, ticket.getPrice()); 
    }

    @Test 
    public void calculateFareBikeWithLessThan30MinutesParkingTime() { 
    
        Date inTime = new Date(); 
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000));  
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime); 
        ticket.setOutTime(outTime); 
        ticket.setParkingSpot(parkingSpot); 
        fareCalculatorService.calculateFare(ticket); 
        assertEquals(0, ticket.getPrice()); 
    }

// Calcul ticket voiture 95%
    @Test 
    public void calculateFareCarWithDiscount() { 
     
        Date inTime = new Date(); 
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));  
        Date outTime = new Date(); 
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false); 
        ticket.setInTime(inTime); 
        ticket.setOutTime(outTime); 
        ticket.setParkingSpot(parkingSpot); 
     
        fareCalculatorService.calculateFare(ticket,true); 
     
        double expectedPrice = 0.95 * ticket.getPrice(); 
        assertEquals(expectedPrice, ticket.getPrice(), 0.1); 
}


//Calcul ticket moto 95%
    @Test 
    public void calculateFareBikeWithDiscount() { 
     
        Date inTime = new Date(); 
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));  
        Date outTime = new Date(); 
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false); 
        ticket.setInTime(inTime); 
        ticket.setOutTime(outTime); 
        ticket.setParkingSpot(parkingSpot); 
     
        fareCalculatorService.calculateFare(ticket,true); 
    
        double expectedPrice = 0.95 * ticket.getPrice(); 
        assertEquals(expectedPrice, ticket.getPrice(), 0.1); 
   }
}
