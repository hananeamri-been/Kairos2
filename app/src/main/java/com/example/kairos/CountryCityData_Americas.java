package com.example.kairos;

import java.util.Arrays;
import java.util.List;

public final class CountryCityData_Americas implements CountryCityData {
    @Override public List<Country> countries() { return Arrays.asList(
            // North America
            new Country("AG","Antigua and Barbuda", Arrays.asList("St. John's","English Harbour","Jolly Harbour","All Saints","Liberta")),
            new Country("BS","Bahamas", Arrays.asList("Nassau","Freeport","George Town","Bimini","Harbour Island")),
            new Country("BB","Barbados", Arrays.asList("Bridgetown","Holetown","Speightstown","Oistins","Bathsheba")),
            new Country("BZ","Belize", Arrays.asList("Belize City","Belmopan","San Ignacio","Placencia","Caye Caulker")),
            new Country("CA","Canada", Arrays.asList("Toronto","Vancouver","Montreal","Quebec City","Calgary","Ottawa","Edmonton")),
            new Country("CR","Costa Rica", Arrays.asList("San José","La Fortuna","Liberia","Tamarindo","Manuel Antonio")),
            new Country("CU","Cuba", Arrays.asList("Havana","Varadero","Trinidad","Santiago de Cuba","Cienfuegos")),
            new Country("DM","Dominica", Arrays.asList("Roseau","Portsmouth","Soufrière","Marigot","Mahaut")),
            new Country("DO","Dominican Republic", Arrays.asList("Santo Domingo","Punta Cana","Puerto Plata","Santiago","La Romana")),
            new Country("SV","El Salvador", Arrays.asList("San Salvador","Santa Ana","Suchitoto","La Libertad","El Tunco")),
            new Country("GD","Grenada", Arrays.asList("St. George's","Grenville","Gouyave","Victoria","Hillsborough")),
            new Country("GT","Guatemala", Arrays.asList("Guatemala City","Antigua Guatemala","Flores","Quetzaltenango","Panajachel")),
            new Country("HT","Haiti", Arrays.asList("Port-au-Prince","Cap-Haïtien","Jacmel","Gonaïves","Les Cayes")),
            new Country("HN","Honduras", Arrays.asList("Tegucigalpa","San Pedro Sula","Roatán","La Ceiba","Copán Ruinas")),
            new Country("JM","Jamaica", Arrays.asList("Kingston","Montego Bay","Ocho Rios","Negril","Port Antonio")),
            new Country("MX","Mexico", Arrays.asList("Mexico City","Cancún","Guadalajara","Monterrey","Tulum","Playa del Carmen","Oaxaca")),
            new Country("NI","Nicaragua", Arrays.asList("Managua","Granada","León","San Juan del Sur","Ometepe")), // <-- note: 'asList' (fix if IDE flags)
            new Country("PA","Panama", Arrays.asList("Panama City","Colón","Boquete","Bocas del Toro","David")),
            new Country("LC","Saint Lucia", Arrays.asList("Castries","Soufrière","Rodney Bay","Gros Islet","Vieux Fort")),
            new Country("TT","Trinidad and Tobago", Arrays.asList("Port of Spain","San Fernando","Scarborough","Chaguanas","Arima")),
            new Country("US","United States", Arrays.asList("New York","Los Angeles","San Francisco","Miami","Chicago","Las Vegas","Boston","Seattle","Washington")),
            // South America
            new Country("AR","Argentina", Arrays.asList("Buenos Aires","Córdoba","Mendoza","Bariloche","Rosario","Ushuaia")),
            new Country("BO","Bolivia", Arrays.asList("La Paz","Sucre","Santa Cruz","Cochabamba","Uyuni")),
            new Country("BR","Brazil", Arrays.asList("Rio de Janeiro","São Paulo","Salvador","Brasília","Florianópolis","Fortaleza","Curitiba")),
            new Country("CL","Chile", Arrays.asList("Santiago","Valparaíso","San Pedro de Atacama","Puerto Varas","Viña del Mar")),
            new Country("CO","Colombia", Arrays.asList("Bogotá","Medellín","Cartagena","Cali","Santa Marta")),
            new Country("EC","Ecuador", Arrays.asList("Quito","Guayaquil","Cuenca","Baños","Galápagos")),
            new Country("GY","Guyana", Arrays.asList("Georgetown","Linden","New Amsterdam","Bartica","Lethem")),
            new Country("PY","Paraguay", Arrays.asList("Asunción","Ciudad del Este","Encarnación","Villarrica","Luque")),
            new Country("PE","Peru", Arrays.asList("Lima","Cusco","Arequipa","Iquitos","Trujillo")),
            new Country("SR","Suriname", Arrays.asList("Paramaribo","Lelydorp","Nieuw Nickerie","Moengo","Albina")),
            new Country("UY","Uruguay", Arrays.asList("Montevideo","Punta del Este","Colonia del Sacramento","Salto","Maldonado")),
            new Country("VE","Venezuela", Arrays.asList("Caracas","Maracaibo","Valencia","Mérida","Isla Margarita"))
    );}
}
