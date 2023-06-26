package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.*;

import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.constants.ParkingType;
import java.util.Date;
import org.mockito.Mockito;

import com.parkit.parkingsystem.constants.Fare;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        Mockito.lenient().when(inputReaderUtil.readSelection()).thenReturn(1);
        Mockito.lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertNotEquals(1,nextAvailableSlot);

    //TODO: vérifiez qu'un ticket est réellement enregistré dans la base de données et que la table Parking est mise à jour avec la disponibilité
   
    }

    @Test
    public void testParkingLotExit(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
       
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        
        Ticket ticket = new Ticket();
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticketDAO.saveTicket(ticket);

        parkingService.processExitingVehicle();

        Ticket ticketRetrieve = ticketDAO.getTicket("ABCDEF");
        assertNotEquals(null, ticketRetrieve.getOutTime());
        assertNotNull(ticketRetrieve.getPrice());
        assertEquals(Fare.CAR_RATE_PER_HOUR ,ticketRetrieve.getPrice(), 0.01); //  0.01 allow to make verification with only 2 digit after decimal
        //TODO: check that the fare generated and out time are populated correctly in the database
    }


    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        testParkingLotExit(); // Insert car and exit car first time

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (120 * 60 * 1000)));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingSpot(parkingService.getNextParkingNumberIfAvailable());

        // Insert ticket into database == simulation of processIncomingVehicle
        ticketDAO.saveTicket(ticket);

        parkingService.processExitingVehicle();

        Ticket savedTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(savedTicket);
        assertEquals(ParkingType.CAR, savedTicket.getParkingSpot().getParkingType());
	    assertEquals(Fare.CAR_RATE_PER_HOUR * 2 * 0.95, savedTicket.getPrice(), 0.01);
        
        int ticketCount = ticketDAO.getNbTicket("ABCDEF");
	    assertEquals(2, ticketCount);
    }

}
