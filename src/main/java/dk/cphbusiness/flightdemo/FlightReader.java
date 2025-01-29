package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        try {
            List<FlightDTO> flightList = getFlightsFromFile("flights.json");
            List<FlightInfoDTO> flightInfoDTOList = getFlightInfoDetails(flightList);
            //System.out.println("Total minutes: ");
            //System.out.println(getTotalAirlineFlightTime("Lufthansa", flightInfoDTOList));
            //flightInfoDTOList.forEach(System.out::println);
            List<FlightInfoDTO> airportToAirport = getFlightsFromTwoAirports("Fukuoka", "Haneda Airport", flightInfoDTOList);
            //airportToAirport.forEach(System.out::println);
            List<FlightInfoDTO> depatureBeforeTime = depatureBeforeSpecificTime(LocalDateTime.of(2024,8, 15, 15, 0), flightInfoDTOList);
            depatureBeforeTime.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FlightDTO> getFlightsFromFile(String filename) throws IOException {

        ObjectMapper objectMapper = Utils.getObjectMapper();

        // Deserialize JSON from a file into FlightDTO[]
        FlightDTO[] flightsArray = objectMapper.readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);

        // Convert to a list
        List<FlightDTO> flightsList = List.of(flightsArray);
        return flightsList;
    }

    public static List<FlightInfoDTO> getFlightInfoDetails(List<FlightDTO> flightList) {
        List<FlightInfoDTO> flightInfoList = flightList.stream()
                .map(flight -> {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    Duration duration = Duration.between(departure, arrival);
                    FlightInfoDTO flightInfo =
                            FlightInfoDTO.builder()
                                    .name(flight.getFlight().getNumber())
                                    .iata(flight.getFlight().getIata())
                                    .airline(flight.getAirline().getName())
                                    .duration(duration)
                                    .departure(departure)
                                    .arrival(arrival)
                                    .origin(flight.getDeparture().getAirport())
                                    .destination(flight.getArrival().getAirport())
                                    .build();

                    return flightInfo;
                })
                .toList();
        return flightInfoList;
    }

    public static long getTotalAirlineFlightTime(String airline, List<FlightInfoDTO> flightList) {
        long totalDuration = flightList.stream()
                .filter(flight -> flight.getAirline() != null)
                .filter(flight -> flight.getAirline().equalsIgnoreCase(airline))
                .mapToLong(flight -> flight.getDuration().toMinutes())
                .sum();
        return totalDuration;
    }

    public static List<FlightInfoDTO> getFlightsFromTwoAirports(String airport1, String airport2, List<FlightInfoDTO> flightList) {
        List<FlightInfoDTO> flightsList = flightList.stream()
                .filter(flight -> flight.getOrigin() != null)
                .filter(flight -> flight.getDestination() != null)
                .filter(flight -> flight.getOrigin().equalsIgnoreCase(airport1) && flight.getDestination().equalsIgnoreCase(airport2))
                .toList();
        return flightsList;
    }
    public static List<FlightInfoDTO> depatureBeforeSpecificTime(LocalDateTime departure, List<FlightInfoDTO> flightList) {
        List<FlightInfoDTO> flightsList = flightList.stream()
                .filter(flight -> flight.getDeparture() != null)
                .filter(flight -> flight.getDeparture().isBefore(departure))
                .toList();
        return flightsList;
    }

    
}
