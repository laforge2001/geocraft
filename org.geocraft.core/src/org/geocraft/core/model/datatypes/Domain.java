/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocraft.core.common.io.TextFile;
import org.geocraft.core.common.util.Sorting;


/**
 * Seismic data is either in the time or the depth domain. 
 */
public enum Domain {
  ABSORBED_DOSE("Absorbed Dose"),
  ACCELERATION("Acceleration"),
  ACIDITY("Acidity"),
  AMOUNT_OF_SUBSTANCE("Amount Of Substance"),
  AMPERES_METERS_SQUARED("Amperes Meters Squared"),
  ANGLE_PER_LENGTH("Angle Per Length"),
  ANGLE_PER_VOLUME("Angle Per Volume"),
  ANGULAR_ACCELERATION("Angular Acceleration"),
  API_NEUTRON("Api Neutron"),
  API_OIL_GRAVITY("Api Oil Gravity"),
  AREA("Area"),
  AREA_PER_TIME("Area Per Time"),
  ATTENUATION_PER_LENGTH("Attenuation Per Length"),
  ATTENUATION_PER_OCTAVE("Attenuation Per Octave"),
  BAUD("Baud"),
  CANDELAS_PER_SQUARE_METER("Candelas Per Square Meter"),
  CAPACITANCE("Capacitance"),
  CAPACITANCE_PER_LENGTH("Capacitance Per Length"),
  CHEMICAL_POTENTIAL("Chemical Potential"),
  CONDUCTIVITY("Conductivity"),
  COULOMB_METERS("Coulomb Meters"),
  COUNTERION_CONDUCTIVITY("Counterion Conductivity"),
  CROSS_SECTION_ABSORPTION("Cross Section Absorption"),
  CUBIC_METERS_PER_STD_CUBIC_METERS_AT_0_DEG_C("Cubic Meters Per Std Cubic Meters At 0 Deg C"),
  CURRENT_DENSITY("Current Density"),
  DARCY_FLOW_COEFFICIENT("Darcy Flow Coefficient"),
  DIGITAL_STORAGE("Digital Storage"),
  DIGITAL_STORAGE_PER_TIME("Digital Storage Per Time"),
  DIMENSIONLESS("Dimensionless"),
  DISTANCE("Distance"),
  DOSE_EQUIVALENT("Dose Equivalent"),
  DOSE_EQUIVALENT_RATE("Dose Equivalent Rate"),
  DYNAMIC_VISCOSITY("Dynamic Viscosity"),
  ELECTRIC_CHARGE("Electric Charge"),
  ELECTRIC_CHARGE_PER_MASS("Electric Charge Per Mass"),
  ELECTRIC_CHARGE_PER_VOLUME("Electric Charge Per Volume"),
  ELECTRIC_CONDUCTANCE("Electric Conductance"),
  ELECTRIC_CURRENT("Electric Current"),
  ELECTRIC_POTENTIAL("Electric Potential"),
  ELECTRIC_POTENTIAL_PER_LENGTH("Electric Potential Per Length"),
  ELECTRIC_RESISTANCE("Electric Resistance"),
  ELECTROCHEMICAL_EQUIVALENT("Electrochemical Equivalent"),
  ENERGY("Energy"),
  EQUIVALENT_PER_MASS("Equivalent Per Mass"),
  EQUIVALENT_PER_VOLUME("Equivalent Per Volume"),
  EXPOSURE_RADIOACTIVITY("Exposure Radioactivity"),
  FLOWRATE("Flowrate"),
  FORCE("Force"),
  FORCE_AREA("Force Area"),
  FORCE_PER_LENGTH("Force Per Length"),
  FORCE_PER_VOLUME("Force Per Volume"),
  FREQUENCY("Frequency"),
  FREQUENCY_INTERVAL("Frequency Interval"),
  GAMMA_RAY_API_UNIT("Gamma Ray Api Unit"),
  GAS_UNIT("Gas Unit"),
  HEAT_CAPACITY("Heat Capacity"),
  HEAT_TRANSFER("Heat Transfer"),
  HEAT_TRANSFER_COEFFICIENT("Heat Transfer Coefficient"),
  ILLUMINANCE("Illuminance"),
  INDUCTANCE("Inductance"),
  INVERSE_AREA("Inverse Area"),
  INVERSE_LENGTH("Inverse Length"),
  INVERSE_PRESSURE("Inverse Pressure"),
  INVERSE_TEMPERATURE("Inverse Temperature"),
  ISOTHERMAL_COMPRESSIBILITY("Isothermal Compressibility"),
  KILOGRAMS_PER_METER_SECOND("Kilograms Per Meter Second"),
  LENGTH_PER_MASS("Length Per Mass"),
  LENGTH_PER_PRESSURE("Length Per Pressure"),
  LENGTH_PER_TEMPERATURE("Length Per Temperature"),
  LEVEL_OF_POWER_INTENSITY("Level Of Power Intensity"),
  LIGHT_EXPOSURE("Light Exposure"),
  LUMENS_PER_WATT("Lumens Per Watt"),
  LUMINOUS_FLUX("Luminous Flux"),
  LUMINOUS_INTENSITY("Luminous Intensity"),
  MAGNETIC_FIELD_STRENGTH("Magnetic Field Strength"),
  MAGNETIC_FLUX("Magnetic Flux"),
  MAGNETIC_FLUX_DENSITY("Magnetic Flux Density"),
  MAGNETIC_FLUX_DENSITY_PER_LENGTH("Magnetic Flux Density Per Length"),
  MAGNETIC_PERMEABILITY("Magnetic Permeability"),
  MAGNETIC_VECTOR_POTENTIAL("Magnetic Vector Potential"),
  MASS("Mass"),
  MASS_ATTENUATION_COEFFICIENT("Mass Attenuation Coefficient"),
  MASS_LENGTH("Mass Length"),
  MASS_PER_AREA("Mass Per Area"),
  MASS_PER_AREA_PER_TIME("Mass Per Area Per Time"),
  MASS_PER_ENERGY("Mass Per Energy"),
  MASS_PER_LENGTH("Mass Per Length"),
  MASS_PER_LENGTH_PER_TIME("Mass Per Length Per Time"),
  MASS_PER_TIME("Mass Per Time"),
  MASS_PER_TIME_PER_AREA("Mass Per Time Per Area"),
  MASS_PER_VOLUME("Mass Per Volume"),
  MASS_PER_VOLUME_PER_LENGTH("Mass Per Volume Per Length"),
  MOBILITY("Mobility"),
  MODULUS_OF_COMPRESSION("Modulus Of Compression"),
  MOLAR_HEAT_CAPACITY("Molar Heat Capacity"),
  MOLAR_VOLUME("Molar Volume"),
  MOLE_PER_AREA("Mole Per Area"),
  MOLE_PER_TIME("Mole Per Time"),
  MOLE_PER_TIME_PER_AREA("Mole Per Time Per Area"),
  MOLE_PER_VOLUME("Mole Per Volume"),
  MOMENTUM("Momentum"),
  MOMENT_OF_INERTIA("Moment Of Inertia"),
  MOMENT_OF_SECTION("Moment Of Section"),
  NON_DARCY_FLOW_COEFFICIENT("Non Darcy Flow Coefficient"),
  NORMALIZED_POWER("Normalized Power"),
  PARACHOR("Parachor"),
  PER_ELECTRIC_POTENTIAL("Per Electric Potential"),
  PER_FORCE("Per Force"),
  PER_MASS("Per Mass"),
  PER_MASS_PER_TIME("Per Mass Per Time"),
  PER_VOLUME("Per Volume"),
  PLANE_ANGLE("Plane Angle"),
  POTENTIAL_DIFFERENCE_PER_POWER_DROP("Potential Difference Per Power Drop"),
  POWER("Power"),
  POWER_PER_AREA("Power Per Area"),
  POWER_PER_VOLUME("Power Per Volume"),
  PRESSURE("Pressure"),
  PRESSURE_PER_LENGTH("Pressure Per Length"),
  PRESSURE_PER_TIME("Pressure Per Time"),
  PRESSURE_SQUARED("Pressure Squared"),
  PRESSURE_TIME_PER_VOLUME("Pressure Time Per Volume"),
  QUANTITY_OF_LIGHT("Quantity Of Light"),
  RADIOACTIVITY("Radioactivity"),
  RELUCTANCE("Reluctance"),
  RESISTIVITY("Resistivity"),
  RESISTIVITY_PER_LENGTH("Resistivity Per Length"),
  ROTATIONAL_VELOCITY("Rotational Velocity"),
  SOLID_ANGLE("Solid Angle"),
  SPECIFIC_ACTIVITY_OF_RADIOACTIVITY("Specific Activity Of Radioactivity"),
  SPECIFIC_ENERGY("Specific Energy"),
  SPECIFIC_HEAT_CAPACITY("Specific Heat Capacity"),
  STANDARD_CUBIC_METERS_AT_0_DEG_CELSIUS("Standard Cubic Meters At 0 Deg Celsius"),
  STANDARD_VOLUME("Standard Volume"),
  STANDARD_VOLUME_PER_AREA("Standard Volume Per Area"),
  STANDARD_VOLUME_PER_TIME("Standard Volume Per Time"),
  STANDARD_VOLUME_PER_VOLUME("Standard Volume Per Volume"),
  STD_CUBIC_METERS_AT_0_DEG_C_PER_CUBIC_METER("Std Cubic Meters At 0 Deg C Per Cubic Meter"),
  STD_CUBIC_METERS_AT_0_DEG_C_PER_SQUARE_METER("Std Cubic Meters At 0 Deg C Per Square Meter"),
  SURFACE_DENSITY_OF_CHARGE("Surface Density Of Charge"),
  TEMPERATURE("Temperature"),
  TEMPERATURE_PER_LENGTH("Temperature Per Length"),
  TEMPERATURE_PER_PRESSURE("Temperature Per Pressure"),
  TEMPERATURE_PER_TIME("Temperature Per Time"),
  THERMAL_CONDUCTANCE("Thermal Conductance"),
  THERMAL_CONDUCTIVITY("Thermal Conductivity"),
  THERMAL_INSULANCE("Thermal Insulance"),
  THERMAL_RESISTANCE("Thermal Resistance"),
  TIME("Time"),
  TIME_PER_LENGTH("Time Per Length"),
  TIME_PER_MASS("Time Per Mass"),
  TIME_PER_VOLUME("Time Per Volume"),
  UNDEFINED("Undefined"),
  VELOCITY("Velocity"),
  VELOCITY_GRADIENT("Velocity Gradient"),
  VOLUME("Volume"),
  VOLUME_DENSITY_OF_CHARGE("Volume Density Of Charge"),
  VOLUME_LENGTH_PER_TIME("Volume Length Per Time"),
  VOLUME_PER_MASS("Volume Per Mass"),
  VOLUME_PER_PASCAL_SECOND_SQUARED("Volume Per Pascal Second Squared"),
  VOLUME_PER_PRESSURE("Volume Per Pressure"),
  VOLUME_PER_ROTATION("Volume Per Rotation"),
  VOLUME_PER_STANDARD_VOLUME("Volume Per Standard Volume"),
  VOLUME_PER_TIME_PER_PRESSURE("Volume Per Time Per Pressure"),
  VOLUME_PER_TIME_PER_TIME("Volume Per Time Per Time"),
  WATTS_PER_CUBIC_METER_KELVIN("Watts Per Cubic Meter Kelvin"),
  WATTS_PER_SQUARE_METER_STERADIAN("Watts Per Square Meter Steradian"),
  WATTS_PER_STERADIAN("Watts Per Steradian"),
  WEBER_METERS("Weber Meters");

  private final String _title;

  Domain(final String title) {
    _title = title;
  }

  @Override
  public String toString() {
    return getTitle();
  }

  public String getTitle() {
    return _title;
  }

  public static Domain fromString(final String title) {
    if (title != null) {
      for (Domain type : Domain.values()) {
        if (title.equals(type._title)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("No Domain \'" + title + "\'");
  }

  public static void main(final String[] args) {
    TextFile file = new TextFile(
        "/home/walucas/dev/workspace/org.geocraft.core.model/src/org/geocraft/core/model/datatypes/Domain.java");
    List<String> domains = new ArrayList<String>();
    for (int i = 18; i < 186; i++) {
      domains.add(file.get(i));
    }
    Collections.sort(domains, Sorting.ALPHANUMERIC_COMPARATOR);
    for (String line : domains) {
      System.out.println(line);
    }
  }

  //  public static void main(final String[] args) {
  //    TextFile file = new TextFile(
  //        "/home/walucas/dev/workspace/org.geocraft.core.model/src/org/geocraft/core/model/datatypes/Unit.java");
  //    TextFile fileOut = new TextFile();
  //    for (int i = 0; i < file.size(); i++) {
  //      String line = file.get(i);
  //      String lineOut = line;
  //      Domain[] domains = Domain.values();
  //      for (Domain domain2 : domains) {
  //        String domain = domain2.getTitle().toUpperCase();
  //        domain = domain.replace(" ", "_");
  //        String oldSeq = "\"" + domain + "\"";
  //        String newSeq = "Domain." + domain;
  //        //System.out.println("OLD SEQ=" + oldSeq + " NEW SEQ=" + newSeq);
  //        lineOut = line.replace(oldSeq, newSeq);
  //        if (line.contains(oldSeq) && !lineOut.contains(oldSeq)) {
  //          System.out.println("LINE " + i + " REPLACED " + oldSeq + " with " + newSeq);
  //        }
  //        line = lineOut;
  //      }
  //      fileOut.add(lineOut);
  //    }
  //    fileOut.write("/home/walucas/foo.txt");
  //  }
  //
  //  public static void mainx(final String[] args) {
  //    List<String> domains = new ArrayList<String>();
  //    for (Unit unit : Unit.values()) {
  //      String domain = unit.getDomain();
  //      if (!domains.contains(domain)) {
  //        domains.add(domain);
  //      }
  //    }
  //    for (int i = 0; i < domains.size(); i++) {
  //      String domain = domains.get(i);
  //      String common = toCommon(domain);
  //      System.out.println(domain + "(\"" + common + "\"),");
  //    }
  //  }
  //
  //  public static String toCommon(final String domain) {
  //    String common = "";
  //    String[] subs = domain.split("_");
  //    for (int i = 0; i < subs.length; i++) {
  //      String sub = subs[i].toLowerCase();
  //      subs[i] = subs[i].substring(0, 1) + sub.substring(1);
  //      common += subs[i];
  //      if (i < subs.length - 1) {
  //        common += " ";
  //      }
  //    }
  //    return common;
  //  }
}