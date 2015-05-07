/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.datatypes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geocraft.core.common.util.Sorting;


/**
 * Enumeration of all the units available.
 */
public enum Unit implements Comparable<Unit>, Serializable {

  UNDEFINED("undefined", "", Domain.UNDEFINED, "", "undefined", Double.NaN, Double.NaN),

  // TODO TRACES is not a unit :-)
  TRACES("trace", "trace", Domain.DIMENSIONLESS, "trace", "trace", 0.0, 1.0),

  SEISMIC_AMPLITUDE("seismic amplitude", "seismic amplitude", Domain.DIMENSIONLESS, "seismic amplitude",
      "seismic amplitude", 0.0, 1.0, true),

  PERCENT("percent", "%", Domain.DIMENSIONLESS, "%", "percent", 0.0, 0.01),

  UNITLESS("unitless", "unitless", Domain.DIMENSIONLESS, "unitless", "unitless", 0.0, 1.0),

  KILOMETER_PER_SECOND("kilometer per second", "km/s", Domain.VELOCITY, "km/s", "kilometer per second", 0.0, 1000.0,
      true),
  VOLUME_FRACTION("volume fraction", "v/v", Domain.DIMENSIONLESS, "v/v", "volume fraction", 0.0, 1.),

  NEWTON_PER_METER_FOURTH_PER_KILOGRAM_METER_CUBED("newton/meter fourth/kilogram meter cubed", "(N/m)4/kg.m3",
      Domain.PARACHOR, "(N/m)4/kg.m3", "newton/meter fourth/kilogram meter cubed", 0.0, 1.0),

  DYNES_PER_CENTIMETER_FOURTH_PER_GRAM_CM_CUBED("dynes/centimeter fourth/gram cm cubed", "(dyne/cm)4/gcm3",
      Domain.PARACHOR, "(dyne/cm)4/gcm3", "dynes/centimeter fourth/gram cm cubed", 0.0, 0.0010),

  INVERSE_KILOGRAMS_PER_SECOND("inverse kilograms per second", "1/(kg.s)", Domain.PER_MASS_PER_TIME, "1/(kg.s)",
      "inverse kilograms per second", 0.0, 1.0),

  INVERSE_HENRY("inverse henry", "1/H", Domain.RELUCTANCE, "1/H", "inverse henry", 0.0, 1.0),

  PER_KELVIN("per Kelvin", "1/K", Domain.INVERSE_TEMPERATURE, "1/K", "per Kelvin", 0.0, 1.0),

  PER_LITRE("per litre", "1/L", Domain.PER_VOLUME, "1/L", "per litre", 0.0, 1000.0),

  PER_NEWTON("per Newton", "1/N", Domain.PER_FORCE, "1/N", "per Newton", 0.0, 1.0),

  PER_PASCAL("per Pascal", "1/Pa", Domain.INVERSE_PRESSURE, "1/Pa", "per Pascal", 0.0, 1.0),

  PER_VOLT("per Volt", "1/V", Domain.PER_ELECTRIC_POTENTIAL, "1/V", "per Volt", 0.0, 1.0),

  PER_ANNUM("per annum", "1/a", Domain.FREQUENCY, "1/a", "per annum", 0.0, 3.16875355494539E-8),

  PER_ANGSTROM("per angstrom", "1/angstrom", Domain.INVERSE_LENGTH, "1/angstrom", "per angstrom", 0.0, 1.0E10),

  PER_BAR("per bar", "1/bar", Domain.INVERSE_PRESSURE, "1/bar", "per bar", 0.0, 1.0E-5),

  PER_BARREL("per barrel", "1/bbl", Domain.PER_VOLUME, "1/bbl", "per barrel", 0.0, 6.28981056977507),

  PER_CENTIMETER("per centimeter", "1/cm", Domain.INVERSE_LENGTH, "1/cm", "per centimeter", 0.0, 100.0),

  PER_DAY("per day", "1/d", Domain.FREQUENCY, "1/d", "per day", 0.0, 1.157407407407407E-5),

  PER_DEGREE_CELSIUS("per degree Celsius", "1/degC", Domain.INVERSE_TEMPERATURE, "1/degC", "per degree Celsius", 0.0,
      1.0),

  PER_DEGREE_FAHRENHEIT("per degree Fahrenheit", "1/degF", Domain.INVERSE_TEMPERATURE, "1/degF",
      "per degree Fahrenheit", 0.0, 1.8),

  PER_DEGREE_RANKINE("per degree Rankine", "1/degR", Domain.INVERSE_TEMPERATURE, "1/degR", "per degree Rankine", 0.0,
      1.8),

  PER_FOOT("per foot", "1/ft", Domain.INVERSE_LENGTH, "1/ft", "per foot", 0.0, 3.28083989501312),

  PER_SQUARE_FOOT("per square foot", "1/ft2", Domain.INVERSE_AREA, "1/ft2", "per square foot", 0.0, 10.76391041670972),

  PER_CUBIC_FOOT("per cubic foot", "1/ft3", Domain.PER_VOLUME, "1/ft3", "per cubic foot", 0.0, 35.31466247128476),

  PER_GRAM("per gram", "1/g", Domain.PER_MASS, "1/g", "per gram", 0.0, 1000.0),

  PER_UK_GALLON("per UK gallon", "1/galUK", Domain.PER_VOLUME, "1/galUK", "per UK gallon", 0.0, 219.25401015584572),

  PER_US_GALLON("per US gallon", "1/galUS", Domain.PER_VOLUME, "1/galUS", "per US gallon", 0.0, 264.1720372841846),

  PER_HOUR("per hour", "1/h", Domain.FREQUENCY, "1/h", "per hour", 0.0, 2.77777777777778E-4),

  PER_INCH("per inch", "1/in", Domain.INVERSE_LENGTH, "1/in", "per inch", 0.0, 39.3700787401575),

  PER_KILOPASCAL("per kilopascal", "1/kPa", Domain.INVERSE_PRESSURE, "1/kPa", "per kilopascal", 0.0, 0.0010),

  PER_KILOGRAM("per kilogram", "1/kg", Domain.PER_MASS, "1/kg", "per kilogram", 0.0, 1.0),

  PER_SQUARE_KILOMETER("per square kilometer", "1/km2", Domain.INVERSE_AREA, "1/km2", "per square kilometer", 0.0,
      1.0E-6),

  PER_POUND_FORCE("per pound force", "1/lbf", Domain.PER_FORCE, "1/lbf", "per pound force", 0.0, 0.22480892365533914),

  PER_POUND("per pound", "1/lbm", Domain.PER_MASS, "1/lbm", "per pound", 0.0, 2.2046224760379585),

  PER_METER("per meter", "1/m", Domain.INVERSE_LENGTH, "1/m", "per meter", 0.0, 1.0),

  PER_SQUARE_METER("per square meter", "1/m2", Domain.INVERSE_AREA, "1/m2", "per square meter", 0.0, 1.0),

  PER_CUBIC_METER("per cubic meter", "1/m3", Domain.PER_VOLUME, "1/m3", "per cubic meter", 0.0, 1.0),

  PER_MILE("per mile", "1/mi", Domain.INVERSE_LENGTH, "1/mi", "per mile", 0.0, 6.21371192237334E-4),

  PER_SQUARE_MILE("per square mile", "1/mi2", Domain.INVERSE_AREA, "1/mi2", "per square mile", 0.0, 3.86102158592535E-7),

  PER_MINUTE("per minute", "1/min", Domain.FREQUENCY, "1/min", "per minute", 0.0, 0.01666666666666667),

  PER_MILLIMETER("per millimeter", "1/mm", Domain.INVERSE_LENGTH, "1/mm", "per millimeter", 0.0, 1000.0),

  PER_NANOMETER("per nanometer", "1/nm", Domain.INVERSE_LENGTH, "1/nm", "per nanometer", 0.0, 1.0E9),

  PER_PICO_PASCAL("per pico pascal", "1/pPa", Domain.INVERSE_PRESSURE, "1/pPa", "per pico pascal", 0.0, 1.0E12),

  PER_POUNDS_PER_SQUARE_INCH("per pounds/square inch", "1/psi", Domain.INVERSE_PRESSURE, "1/psi",
      "per pounds/square inch", 0.0, 1.450377438972831E-4),

  PER_SECOND("per second", "1/s", Domain.FREQUENCY, "1/s", "per second", 0.0, 1.0),

  PER_MICROVOLT("per microvolt", "1/uV", Domain.PER_ELECTRIC_POTENTIAL, "1/uV", "per microvolt", 0.0, 1000000.0),

  PER_MICRO_POUNDS_PER_SQUARE_INCH("per micro pounds per square inch", "1/upsi", Domain.INVERSE_PRESSURE, "1/upsi",
      "per micro pounds per square inch", 0.0, 145.03774389728312),

  PER_WEEK("per week", "1/wk", Domain.FREQUENCY, "1/wk", "per week", 0.0, 1.653439153439154E-6),

  PER_YARD("per yard", "1/yd", Domain.INVERSE_LENGTH, "1/yd", "per yard", 0.0, 1.093613298337708),

  THOUSAND_CUBIC_FEET("thousand cubic feet", "1000ft3", Domain.VOLUME, "1000ft3", "thousand cubic feet", 0.0,
      28.316846592),

  THOUSAND_CUBIC_FEET_PER_BARREL("thousand cubic feet per barrel", "1000ft3/bbl", Domain.DIMENSIONLESS, "1000ft3/bbl",
      "thousand cubic feet per barrel", 0.0, 178.10760099706079),

  THOUSAND_CUBIC_FEET_PER_DAY("thousand cubic feet per day", "1000ft3/d", Domain.FLOWRATE, "1000ft3/d",
      "thousand cubic feet per day", 0.0, 3.277413194444444E-4),

  THOUSAND_CUBIC_FEET_PER_DAY_PER_FOOT("thousand cubic feet per day per foot", "1000ft3/d.ft", Domain.AREA_PER_TIME,
      "1000ft3/d.ft", "thousand cubic feet per day per foot", 0.0, 0.0010752666666666668),

  THOUSAND_CUBIC_FEET_PER_DAY_PER_PSI("thousand cubic feet per day per psi", "1000ft3/psi.d",
      Domain.VOLUME_PER_TIME_PER_PRESSURE, "1000ft3/psi.d", "thousand cubic feet per day per psi", 0.0,
      4.7534861554140985E-8),

  THOUSAND_CUBIC_METERS_PER_DAY("thousand cubic meters per day", "1000m3/d", Domain.FLOWRATE, "1000m3/d",
      "thousand cubic meters per day", 0.0, 0.011574074074074073),

  THOUSAND_CUBIC_METER_PER_DAY_PER_METER("thousand cubic meter per day per meter", "1000m3/d.m", Domain.AREA_PER_TIME,
      "1000m3/d.m", "thousand cubic meter per day per meter", 0.0, 0.011574074074074073),

  THOUSAND_CUBIC_METERS_PER_HOUR("thousand cubic meters per hour", "1000m3/h", Domain.FLOWRATE, "1000m3/h",
      "thousand cubic meters per hour", 0.0, 0.2777777777777778),

  THOUSAND_CUBIC_METERS_PER_HOUR_PER_METER("thousand cubic meters per hour per meter", "1000m3/h.m",
      Domain.AREA_PER_TIME, "1000m3/h.m", "thousand cubic meters per hour per meter", 0.0, 0.2777777777777778),

  THOUSAND_CUBIC_METER_PER_DAY_METER("thousand (cubic meter per day)-meter", "1000m4/d", Domain.VOLUME_LENGTH_PER_TIME,
      "1000m4/d", "thousand (cubic meter per day)-meter", 0.0, 0.011574074074074073),

  TEN_THOUSAND_KILOGRAMS_PER_CUBIC_METER("ten thousand kilograms per cubic meter", "10Mg/m3", Domain.MASS_PER_VOLUME,
      "10Mg/m3", "ten thousand kilograms per cubic meter", 0.0, 10000.0),

  AMPERE("ampere", "A", Domain.ELECTRIC_CURRENT, "A", "ampere", 0.0, 1.0),

  AMPERE_HOUR("Ampere hour", "A.h", Domain.ELECTRIC_CHARGE, "A.h", "Ampere hour", 0.0, 3600.0),

  AMPERES_METERS_SQUARED("amperes meters squared", "A.m2", Domain.AMPERES_METERS_SQUARED, "A.m2",
      "amperes meters squared", 0.0, 1.0),

  AMPERE_SECOND("Ampere second", "A.s", Domain.ELECTRIC_CHARGE, "A.s", "Ampere second", 0.0, 1.0),

  AMPERE_SECONDS_PER_KILOGRAM("ampere seconds per kilogram", "A.s/kg", Domain.ELECTRIC_CHARGE_PER_MASS, "A.s/kg",
      "ampere seconds per kilogram", 0.0, 1.0),

  AMPERE_SECONDS_PER_CUBIC_METER("ampere seconds per cubic meter", "A.s/m3", Domain.ELECTRIC_CHARGE_PER_VOLUME,
      "A.s/m3", "ampere seconds per cubic meter", 0.0, 1.0),

  AMPERE_PER_SQUARE_CENTIMETER("ampere per square centimeter", "A/cm2", Domain.CURRENT_DENSITY, "A/cm2",
      "ampere per square centimeter", 0.0, 10000.0),

  AMPERE_PER_SQUARE_FOOT("ampere per square foot", "A/ft2", Domain.CURRENT_DENSITY, "A/ft2", "ampere per square foot",
      0.0, 10.763910416709722),

  AMPERES_PER_METER("amperes/meter", "A/m", Domain.MAGNETIC_FIELD_STRENGTH, "A/m", "amperes/meter", 0.0, 1.0),

  AMPERES_PER_SQUARE_METER("amperes/square meter", "A/m2", Domain.CURRENT_DENSITY, "A/m2", "amperes/square meter", 0.0,
      1.0),

  AMPERE_PER_MILLIMETER("Ampere/millimeter", "A/mm", Domain.MAGNETIC_FIELD_STRENGTH, "A/mm", "Ampere/millimeter", 0.0,
      1000.0),

  AMPERE_PER_SQUARE_MILLIMETER("Ampere/square millimeter", "A/mm2", Domain.CURRENT_DENSITY, "A/mm2",
      "Ampere/square millimeter", 0.0, 1000000.0),

  BEL("bel", "B", Domain.LEVEL_OF_POWER_INTENSITY, "B", "bel", 0.0, 1.0),

  BELS_PER_OCTAVE("bels/octave", "B/O", Domain.ATTENUATION_PER_OCTAVE, "B/O", "bels/octave", 0.0, 10.0),

  BELS_PER_METER("bels/meter", "B/m", Domain.ATTENUATION_PER_LENGTH, "B/m", "bels/meter", 0.0, 10.0),

  BAUD("baud", "Bd", Domain.BAUD, "Bd", "baud", 0.0, 1.0),

  BECQUEREL("becquerel", "Bq", Domain.RADIOACTIVITY, "Bq", "becquerel", 0.0, 1.0),

  BECQUEREL_PER_KILOGRAM("becquerel per kilogram", "Bq/kg", Domain.SPECIFIC_ACTIVITY_OF_RADIOACTIVITY, "Bq/kg",
      "becquerel per kilogram", 0.0, 1.0),

  BRITISH_THERMAL_UNIT("British thermal unit", "Btu", Domain.ENERGY, "Btu", "British thermal unit", 0.0, 1055.056),

  MILLION_BTUS("million Btus", "Btu(million)", Domain.ENERGY, "Btu(million)", "million Btus", 0.0, 1.055056E9),

  MILLION_BTUS_PER_HOUR("million Btus/hour", "Btu(million)/hr", Domain.POWER, "Btu(million)/hr", "million Btus/hour",
      0.0, 293071.1),

  BTUS_PER_HOUR_FOOT_SQUARED_DEG_F_PER_INCH("Btus/hour foot squared deg F per inch", "Btu.in/hr.ft2.F",
      Domain.THERMAL_CONDUCTIVITY, "Btu.in/hr.ft2.F", "Btus/hour foot squared deg F per inch", 0.0, 0.1442279),

  BRITISH_THERMAL_UNITS_PER_BARREL("British thermal units/barrel", "Btu/bbl", Domain.MODULUS_OF_COMPRESSION, "Btu/bbl",
      "British thermal units/barrel", 0.0, 6636.10238050461),

  BTUS_PER_BRAKE_HORSEPOWER_HOUR("Btus/brake-horsepower hour", "Btu/bhp.hr", Domain.DIMENSIONLESS, "Btu/bhp.hr",
      "Btus/brake-horsepower hour", 0.0, 3.930148E-4),

  BRITISH_THERMAL_UNITS_PER_CUBIC_FOOT("British thermal units/cubic foot", "Btu/ft3", Domain.MODULUS_OF_COMPRESSION,
      "Btu/ft3", "British thermal units/cubic foot", 0.0, 37258.95),

  BRITISH_THERMAL_UNITS_PER_UK_GALLON("British thermal units/U.K. gallon", "Btu/galUK", Domain.MODULUS_OF_COMPRESSION,
      "Btu/galUK", "British thermal units/U.K. gallon", 0.0, 232080.0),

  BRITISH_THERMAL_UNITS_PER_US_GALLON("British thermal units/U.S. gallon", "Btu/galUS", Domain.MODULUS_OF_COMPRESSION,
      "Btu/galUS", "British thermal units/U.S. gallon", 0.0, 278716.3),

  BRITISH_THERMAL_UNIT_PER_HOUR("British thermal unit/hour", "Btu/hr", Domain.POWER, "Btu/hr",
      "British thermal unit/hour", 0.0, 0.2930711),

  BRITISH_THERMAL_UNITS_PER_HOUR_FOOT_DEG_F("British thermal units/hour foot deg F", "Btu/hr.ft.degF",
      Domain.THERMAL_CONDUCTIVITY, "Btu/hr.ft.degF", "British thermal units/hour foot deg F", 0.0, 1.730735),

  BTUS_PER_HOUR_PER_SQUARE_FOOT("Btus/hour per square foot", "Btu/hr.ft2", Domain.POWER_PER_AREA, "Btu/hr.ft2",
      "Btus/hour per square foot", 0.0, 3.154591),

  BTUS_PER_HOUR_FOOT_SQUARED_DEG_F("Btus/hour foot squared deg F", "Btu/hr.ft2.degF", Domain.HEAT_TRANSFER_COEFFICIENT,
      "Btu/hr.ft2.degF", "Btus/hour foot squared deg F", 0.0, 5.678263),

  BTUS_PER_HOUR_FOOT_SQUARED_DEG_R("Btus/hour foot squared deg R", "Btu/hr.ft2.degR", Domain.HEAT_TRANSFER_COEFFICIENT,
      "Btu/hr.ft2.degR", "Btus/hour foot squared deg R", 0.0, 5.678263),

  BRITISH_THERMAL_UNITS_PER_HOUR_CUBIC_FOOT("British thermal units/hour cubic foot", "Btu/hr.ft3",
      Domain.POWER_PER_VOLUME, "Btu/hr.ft3", "British thermal units/hour cubic foot", 0.0, 10.34971),

  BTUS_PER_HOUR_FOOT_CUBED_DEG_F("Btus/hour foot cubed deg F", "Btu/hr.ft3.degF", Domain.WATTS_PER_CUBIC_METER_KELVIN,
      "Btu/hr.ft3.degF", "Btus/hour foot cubed deg F", 0.0, 18.62947),

  BTUS_PER_HOUR_METER_SQUARED_DEG_C("Btus/hour meter squared deg C", "Btu/hr.m2.degC",
      Domain.HEAT_TRANSFER_COEFFICIENT, "Btu/hr.m2.degC", "Btus/hour meter squared deg C", 0.0, 0.2930711),

  BRITISH_THERMAL_UNITS_PER_POUND_MASS("British thermal units/pound mass", "Btu/lbm", Domain.SPECIFIC_ENERGY,
      "Btu/lbm", "British thermal units/pound mass", 0.0, 2326.0),

  BRITISH_THERMAL_UNITS_PER_POUND_MASS_DEG_F("British thermal units/pound mass deg F", "Btu/lbm.degF",
      Domain.SPECIFIC_HEAT_CAPACITY, "Btu/lbm.degF", "British thermal units/pound mass deg F", 0.0, 4186.8),

  BRITISH_THERMAL_UNITS_PER_POUND_MASS_DEG_R("British thermal units/pound mass deg R", "Btu/lbm.degR",
      Domain.SPECIFIC_HEAT_CAPACITY, "Btu/lbm.degR", "British thermal units/pound mass deg R", 0.0, 4186.8),

  BRITISH_THERMAL_UNITS_PER_MINUTE("British thermal units/minute", "Btu/min", Domain.POWER, "Btu/min",
      "British thermal units/minute", 0.0, 17.58427),

  BRITISH_THERMAL_UNITS_PER_POUND_MASS_MOL("British thermal units/pound mass mol", "Btu/mol(lbm)",
      Domain.CHEMICAL_POTENTIAL, "Btu/mol(lbm)", "British thermal units/pound mass mol", 0.0, 2326.0),

  BTUS_PER_POUND_MASS_MOL_DEG_F("Btus/pound mass mol deg F", "Btu/mol(lbm).F", Domain.MOLAR_HEAT_CAPACITY,
      "Btu/mol(lbm).degF", "Btus/pound mass mol deg F", 0.0, 4186.8),

  BRITISH_THERMAL_UNITS_PER_SECOND("British thermal units/second", "Btu/s", Domain.POWER, "Btu/s",
      "British thermal units/second", 0.0, 1055.056),

  BRITISH_THERMAL_UNITS_PER_SECOND_SQUARE_FOOT("British thermal units/second square foot", "Btu/s.ft2",
      Domain.POWER_PER_AREA, "Btu/s.ft2", "British thermal units/second square foot", 0.0, 11356.53),

  BTUS_PER_SECOND_PER_SQUARE_FOOT_DEG_F("Btus/second per square foot deg F", "Btu/s.ft2.degF",
      Domain.HEAT_TRANSFER_COEFFICIENT, "Btu/s.ft2.degF", "Btus/second per square foot deg F", 0.0, 20441.75),

  BTUS_PER_SECOND_PER_CUBIC_FOOT("Btus/second per cubic foot", "Btu/s.ft3", Domain.POWER_PER_VOLUME, "Btu/s.ft3",
      "Btus/second per cubic foot", 0.0, 37258.95),

  BTUS_PER_SECOND_PER_CUBIC_FOOT_DEG_F("Btus/second per cubic foot deg F", "Btu/s.ft3.degF",
      Domain.WATTS_PER_CUBIC_METER_KELVIN, "Btu/s.ft3.degF", "Btus/second per cubic foot deg F", 0.0, 67066.11),

  COULOMB("coulomb", "C", Domain.ELECTRIC_CHARGE, "C", "coulomb", 0.0, 1.0),

  COULOMB_METERS("coulomb meters", "C.m", Domain.COULOMB_METERS, "C.m", "coulomb meters", 0.0, 1.0),

  COULOMBS_PER_SQUARE_CENTIMETER("Coulombs/square centimeter", "C/cm2", Domain.VOLUME_DENSITY_OF_CHARGE, "C/cm2",
      "Coulombs/square centimeter", 0.0, 10000.0),

  COULOMBS_PER_CUBIC_CENTIMETER("Coulombs/cubic centimeter", "C/cm3", Domain.SURFACE_DENSITY_OF_CHARGE, "C/cm3",
      "Coulombs/cubic centimeter", 0.0, 1000000.0),

  COULOMB_PER_GRAM("coulomb per gram", "C/g", Domain.EXPOSURE_RADIOACTIVITY, "C/g", "coulomb per gram", 0.0, 1000.0),

  COULOMB_PER_KILOGRAM("coulomb per kilogram", "C/kg", Domain.EXPOSURE_RADIOACTIVITY, "C/kg", "coulomb per kilogram",
      0.0, 1.0),

  COULOMBS_PER_SQUARE_METER("coulombs/square meter", "C/m2", Domain.VOLUME_DENSITY_OF_CHARGE, "C/m2",
      "coulombs/square meter", 0.0, 1.0),

  COULOMBS_PER_CUBIC_METER("coulombs/cubic meter", "C/m3", Domain.SURFACE_DENSITY_OF_CHARGE, "C/m3",
      "coulombs/cubic meter", 0.0, 1.0),

  COULOMBS_PER_SQUARE_MILLIMETER("Coulombs/square millimeter", "C/mm2", Domain.VOLUME_DENSITY_OF_CHARGE, "C/mm2",
      "Coulombs/square millimeter", 0.0, 1000000.0),

  COULOMBS_PER_CUBIC_MILLIMETER("Coulombs/cubic millimeter", "C/mm3", Domain.SURFACE_DENSITY_OF_CHARGE, "C/mm3",
      "Coulombs/cubic millimeter", 0.0, 1.0E9),

  CHEVAL_VAPEUR("cheval vapeur", "CV", Domain.POWER, "CV", "cheval vapeur", 0.0, 735.499),

  CV_HOURS("CV hours", "CV.h", Domain.ENERGY, "CV.h", "CV hours", 0.0, 2647796.0),

  CHUS("chus", "Chu", Domain.ENERGY, "Chu", "chus", 0.0, 1899.101),

  CURIE("curie", "Ci", Domain.RADIOACTIVITY, "Ci", "curie", 0.0, 3.7E10),

  DARCY("darcy", "D", Domain.AREA, "D", "darcy", 0.0, 9.86923E-13),

  DARCY_FOOT("darcy foot", "D.ft", Domain.VOLUME, "D.ft", "darcy foot", 0.0, 3.008141E-13),

  DARCY_METER("darcy meter", "D.m", Domain.VOLUME, "D.m", "darcy meter", 0.0, 9.86923E-13),

  EXAAMP("exaamp", "EA", Domain.ELECTRIC_CURRENT, "EA", "exaamp", 0.0, 1.0E18),

  EXACOULOMB("exacoulomb", "EC", Domain.ELECTRIC_CHARGE, "EC", "exacoulomb", 0.0, 1.0E18),

  EXAEUCLID("exaeuclid", "EEuc", Domain.DIMENSIONLESS, "EEuc", "exa euclid", 0.0, 1.0E18),

  EXAFARAD("exafarad", "EF", Domain.CAPACITANCE, "EF", "exafarad", 0.0, 1.0E18),

  EXAGRAY("exagray", "EGy", Domain.ABSORBED_DOSE, "EGy", "exagray", 0.0, 1.0E18),

  EXAHENRY("exahenry", "EH", Domain.INDUCTANCE, "EH", "exahenry", 0.0, 1.0E18),

  EXAHERTZ("exahertz", "EHz", Domain.ROTATIONAL_VELOCITY, "EHz", "exahertz", 0.0, 6.2831853070000005E18),

  EXAJOULE("exajoule", "EJ", Domain.ENERGY, "EJ", "exajoule", 0.0, 1.0E18),

  EXAJOULES_PER_YEAR("exajoules/year", "EJ/a", Domain.POWER, "EJ/a", "exajoules/year", 0.0, 3.168754E10),

  EXANEWTON("exanewton", "EN", Domain.FORCE, "EN", "exanewton", 0.0, 1.0E18),

  EXAPOISE("exapoise", "EP", Domain.DYNAMIC_VISCOSITY, "EP", "exapoise", 0.0, 1.0E17),

  EXAPASCAL("exapascal", "EPa", Domain.PRESSURE, "EPa", "exapascal", 0.0, 1.0E18),

  EXASIEMEN("exasiemen", "ES", Domain.ELECTRIC_CONDUCTANCE, "ES", "exasiemen", 0.0, 1.0E18),

  EXATESLA("exatesla", "ET", Domain.MAGNETIC_FLUX_DENSITY, "ET", "exatesla", 0.0, 1.0E18),

  EXAWATT("exawatt", "EW", Domain.POWER, "EW", "exawatt", 0.0, 1.0E18),

  EXAWEBER("exaweber", "EWb", Domain.MAGNETIC_FLUX, "EWb", "exaweber", 0.0, 1.0E18),

  EXAYEAR("exayear", "Ea", Domain.TIME, "Ea", "exayear", 0.0, 3.155815E25),

  EXACALORIE("exacalorie", "Ecal", Domain.ENERGY, "Ecal", "exacalorie", 0.0, 4.184E18),

  EXAELECTRON_VOLTS("exaelectron-volts", "EeV", Domain.ENERGY, "EeV", "exaelectron-volts", 0.0, 0.160219),

  EXAGRAM("exagram", "Eg", Domain.MASS, "Eg", "exagram", 0.0, 1.0E15),

  EXAGAMMA("exagamma", "Egamma", Domain.MAGNETIC_FIELD_STRENGTH, "Egamma", "exagamma", 0.0, 7.957747E14),

  EXAGAUSS("exagauss", "Egauss", Domain.MAGNETIC_FLUX_DENSITY, "Egauss", "exagauss", 0.0, 1.0E14),

  EXAMETER("exameter", "Em", Domain.DISTANCE, "Em", "exameter", 0.0, 1.0E18),

  EXAMHO("examho", "Emho", Domain.ELECTRIC_CONDUCTANCE, "Emho", "examho", 0.0, 1.0E18),

  EXAOHM("exaohm", "Eohm", Domain.ELECTRIC_RESISTANCE, "Eohm", "exaohm", 0.0, 1.0E18),

  EXARAD("exarad", "Erd", Domain.ABSORBED_DOSE, "Erd", "exarad", 0.0, 1.0E16),

  EUCLID("euclid", "Euc", Domain.DIMENSIONLESS, "Euc", "euclid", 0.0, 1.0),

  FARAD("farad", "F", Domain.CAPACITANCE, "F", "farad", 0.0, 1.0),

  FARADS_PER_METER("farads/meter", "F/m", Domain.CAPACITANCE_PER_LENGTH, "F/m", "farads/meter", 0.0, 1.0),

  GIGAAMP("gigaamp", "GA", Domain.ELECTRIC_CURRENT, "GA", "gigaamp", 0.0, 1.0E9),

  GIGABECQUEREL("gigabecquerel", "GBq", Domain.RADIOACTIVITY, "GBq", "gigabecquerel", 0.0, 1.0E9),

  GIGACOULOMB("gigacoulomb", "GC", Domain.ELECTRIC_CHARGE, "GC", "gigacoulomb", 0.0, 1.0E9),

  GIGAEUCLID("gigaeuclid", "GEuc", Domain.DIMENSIONLESS, "GEuc", "giga euclid", 0.0, 1.0E9),

  GIGAFARAD("gigafarad", "GF", Domain.CAPACITANCE, "GF", "gigafarad", 0.0, 1.0E9),

  GIGAGRAY("gigagray", "GGy", Domain.ABSORBED_DOSE, "GGy", "gigagray", 0.0, 1.0E9),

  GIGAHENRY("gigahenry", "GH", Domain.INDUCTANCE, "GH", "gigahenry", 0.0, 1.0E9),

  GIGAHERTZ("gigahertz", "GHz", Domain.ROTATIONAL_VELOCITY, "GHz", "gigahertz", 0.0, 6.283185307E9),

  GIGAJOULE("gigajoule", "GJ", Domain.ENERGY, "GJ", "gigajoule", 0.0, 1.0E9),

  GIGANEWTON("giganewton", "GN", Domain.FORCE, "GN", "giganewton", 0.0, 1.0E9),

  GIGAPOISE("gigapoise", "GP", Domain.DYNAMIC_VISCOSITY, "GP", "gigapoise", 0.0, 1.0E8),

  GIGAPASCAL("gigapascal", "GPa", Domain.PRESSURE, "GPa", "gigapascal", 0.0, 1.0E9),

  GIGAPASCAL_PER_CENTIMETER("gigapascal per centimeter", "GPa/cm", Domain.FORCE_PER_VOLUME, "GPa/cm",
      "gigapascal per centimeter", 0.0, 1.0E11),

  GIGAPASCAL_SQUARED("gigapascal squared", "GPa2", Domain.PRESSURE_SQUARED, "GPa2", "gigapascal squared", 0.0, 1.0E18),

  GIGASIEMENS("gigasiemens", "GS", Domain.ELECTRIC_CONDUCTANCE, "GS", "gigasiemens", 0.0, 1.0E9),

  GIGATESLA("gigatesla", "GT", Domain.MAGNETIC_FLUX_DENSITY, "GT", "gigatesla", 0.0, 1.0E9),

  GIGAVOLT("gigavolt", "GV", Domain.ELECTRIC_POTENTIAL, "GV", "gigavolt", 0.0, 1.0E9),

  GIGAWATT("gigawatt", "GW", Domain.POWER, "GW", "gigawatt", 0.0, 1.0E9),

  GIGAWATT_HOUR("gigawatt hour", "GW.h", Domain.ENERGY, "GW.h", "gigawatt hour", 0.0, 3.6E12),

  GIGAWEBER("gigaweber", "GWb", Domain.MAGNETIC_FLUX, "GWb", "gigaweber", 0.0, 1.0E9),

  GIGAYEARS("gigayears", "Ga", Domain.TIME, "Ga", "gigayears", 0.0, 3.155815E16),

  GALILEO("galileo", "Gal", Domain.ACCELERATION, "Gal", "galileo", 0.0, 0.01),

  GIGACALORIE("gigacalorie", "Gcal", Domain.ENERGY, "Gcal", "gigacalorie", 0.0, 4.184E9),

  BILLIONS_OF_ELECTRON_VOLTS("billions of electron volts", "GeV", Domain.ENERGY, "GeV", "billions of electron volts",
      0.0, 1.60219E-10),

  GIGAGRAM("gigagram", "Gg", Domain.MASS, "Gg", "gigagram", 0.0, 1000000.0),

  GIGAGAMMA("gigagamma", "Ggamma", Domain.MAGNETIC_FIELD_STRENGTH, "Ggamma", "gigagamma", 0.0, 7.957747E-5),

  GIGAGAUSS("gigagauss", "Ggauss", Domain.MAGNETIC_FLUX_DENSITY, "Ggauss", "gigagauss", 0.0, 100000.0),

  GIGAMETER("gigameter", "Gm", Domain.DISTANCE, "Gm", "gigameter", 0.0, 1.0E9),

  GIGAMHO("gigamho", "Gmho", Domain.ELECTRIC_CONDUCTANCE, "Gmho", "gigamho", 0.0, 1.0E9),

  GIGAOHM("gigaohm", "Gohm", Domain.ELECTRIC_RESISTANCE, "Gohm", "gigaohm", 0.0, 1.0E9),

  GIGARAD("gigarad", "Grd", Domain.ABSORBED_DOSE, "Grd", "gigarad", 0.0, 1.0E7),

  GIGA_STANDARD_CUBIC_METERS_15C("giga standard cubic meters 15C", "Gsm3", Domain.STANDARD_VOLUME, "Gsm3",
      "giga standard cubic meters 15C", 0.0, 1.0E9),

  GRAY("gray", "Gy", Domain.ABSORBED_DOSE, "Gy", "gray", 0.0, 1.0),

  HENRY("henry", "H", Domain.INDUCTANCE, "H", "henry", 0.0, 1.0),

  HENRIES_PER_METER("henries/meter", "H/m", Domain.MAGNETIC_PERMEABILITY, "H/m", "henries/meter", 0.0, 1.0),

  HERTZ("Hertz", "Hz", Domain.ROTATIONAL_VELOCITY, "Hz", "Hertz", 0.0, 6.283185307),

  JOULE("joule", "J", Domain.ENERGY, "J", "joule", 0.0, 1.0),

  JOULE_PER_SECOND_PER_DEGREE_CELSIUS_PER_SQUARE_METER("joule per second per degree celsius per square meter",
      "J/(s.degC.m2)", Domain.HEAT_TRANSFER, "J/(s.degC.m2)", "joule per second per degree celsius per square meter",
      0.0, 1.0),

  JOULES_PER_DELTA_KELVIN("joules per delta kelvin", "J/K", Domain.HEAT_CAPACITY, "J/K", "joules per delta kelvin",
      0.0, 1.0),

  JOULES_PER_SQUARE_CENTIMETER("joules/square centimeter", "J/cm2", Domain.FORCE_PER_LENGTH, "J/cm2",
      "joules/square centimeter", 0.0, 10000.0),

  JOULES_PER_CUBIC_DECIMETER("joules/cubic decimeter", "J/dm3", Domain.MODULUS_OF_COMPRESSION, "J/dm3",
      "joules/cubic decimeter", 0.0, 1000.0),

  JOULES_PER_GRAM("joules/gram", "J/g", Domain.SPECIFIC_ENERGY, "J/g", "joules/gram", 0.0, 1000.0),

  JOULES_PER_GRAM_DEGREE_KELVIN("joules/gram degree Kelvin", "J/g.K", Domain.SPECIFIC_HEAT_CAPACITY, "J/g.K",
      "joules/gram degree Kelvin", 0.0, 1000.0),

  JOULES_PER_KILOGRAM("joules/kilogram", "J/kg", Domain.SPECIFIC_ENERGY, "J/kg", "joules/kilogram", 0.0, 1.0),

  JOULES_PER_KILOGRAM_DEGREE_KELVIN("joules/kilogram degree kelvin", "J/kg.K", Domain.SPECIFIC_HEAT_CAPACITY, "J/kg.K",
      "joules/kilogram degree kelvin", 0.0, 1.0),

  JOULES_PER_METER("joules/meter", "J/m", Domain.FORCE, "J/m", "joules/meter", 0.0, 1.0),

  JOULES_PER_SQUARE_METER("joules/square meter", "J/m2", Domain.FORCE_PER_LENGTH, "J/m2", "joules/square meter", 0.0,
      1.0),

  JOULES_PER_CUBIC_METER("joules/cubic meter", "J/m3", Domain.MODULUS_OF_COMPRESSION, "J/m3", "joules/cubic meter",
      0.0, 1.0),

  JOULES_PER_MOLE("joules/mole", "J/mol", Domain.CHEMICAL_POTENTIAL, "J/mol", "joules/mole", 0.0, 1.0),

  JOULES_PER_MOLE_DEGREE_KELVIN("joules/mole degree kelvin", "J/mol.K", Domain.MOLAR_HEAT_CAPACITY, "J/mol.K",
      "joules/mole degree kelvin", 0.0, 1.0),

  KELVIN("kelvin", "K", Domain.TEMPERATURE, "K", "kelvin", 0.0, 1.0),

  KELVIN_METERS_SQUARED_PER_WATT("kelvin meters squared/watt", "K.m2/W", Domain.THERMAL_INSULANCE, "K.m2/W",
      "kelvin meters squared/watt", 0.0, 1.0),

  DEGREES_KELVIN_SQUARE_METERS_PER_KILOWATT("degrees Kelvin square meters/kilowatt", "K.m2/kW",
      Domain.THERMAL_INSULANCE, "K.m2/kW", "degrees Kelvin square meters/kilowatt", 0.0, 0.0010),

  DEGREE_KELVIN_PER_PASCAL("degree kelvin per Pascal", "K/Pa", Domain.TEMPERATURE_PER_PRESSURE, "K.m.s2/kg",
      "degree kelvin per Pascal", 0.0, 1.0),

  DELTA_KELVIN_PER_WATT("delta kelvin per watt", "K/W", Domain.THERMAL_RESISTANCE, "K/W", "delta kelvin per watt", 0.0,
      1.0),

  DEGREES_KELVIN_PER_METER("degrees kelvin/meter", "K/m", Domain.TEMPERATURE_PER_LENGTH, "K/m", "degrees kelvin/meter",
      0.0, 1.0),

  KELVIN_PER_SECOND("kelvin per second", "K/s", Domain.TEMPERATURE_PER_TIME, "K/s", "kelvin per second", 0.0, 1.0),

  LITRE("litre", "L", Domain.VOLUME, "L", "litre", 0.0, 0.0010),

  LITER_PER_HUNDRED_KILOGRAM("liter per hundred kilogram", "L/100kg", Domain.VOLUME_PER_MASS, "L/100kg", "L/100kg",
      0.0, 1.0E-5),

  LITRES_PER_100_KILOMETERS("litres/100 kilometers", "L/100km", Domain.AREA, "L/100km", "litres/100 kilometers", 0.0,
      1.0E-9),

  LITER_PER_TEN_BARREL("liter per ten barrel", "L/10bbl", Domain.DIMENSIONLESS, "L/10bbl", "liter per ten barrel", 0.0,
      6.289810569775069E-4),

  LITRES_PER_MINUTE_PER_BAR("litres per minute per bar", "L/bar.min", Domain.VOLUME_PER_TIME_PER_PRESSURE, "L/bar.min",
      "litres per minute per bar", 0.0, 1.6666666666666666E-10),

  LITER_PER_HOUR("liter per hour", "L/h", Domain.FLOWRATE, "L/h", "liter per hour", 0.0, 2.7777777777777776E-7),

  LITER_PER_KILOGRAM("liter per kilogram", "L/kg", Domain.VOLUME_PER_MASS, "L/kg", "liter per kilogram", 0.0, 0.0010),

  LITRES_PER_METER("litres/meter", "L/m", Domain.AREA, "L/m", "litres/meter", 0.0, 0.0010),

  LITRES_PER_CUBIC_METER("litres/cubic meter", "L/m3", Domain.DIMENSIONLESS, "L/m3", "litres/cubic meter", 0.0, 0.0010),

  LITER_PER_MINUTE("liter per minute", "L/min", Domain.FLOWRATE, "L/min", "liter per minute", 0.0,
      1.6666666666666667E-5),

  LITRES_PER_MOLE_GRAM("litres/mole (gram)", "L/mol(g)", Domain.MOLAR_VOLUME, "L/mol(g)", "litres/mole (gram)", 0.0,
      1.0),

  LITRES_PER_MOLE_KILOGRAM("litres/mole (kilogram)", "L/mol(kg)", Domain.MOLAR_VOLUME, "L/mol(kg)",
      "litres/mole (kilogram)", 0.0, 0.0010),

  LITRES_PER_SECOND("litres/second", "L/s", Domain.FLOWRATE, "L/s", "litres/second", 0.0, 0.0010),

  LITRES_PER_SECOND_PER_SECOND("litres/second/second", "L/s2", Domain.VOLUME_PER_TIME_PER_TIME, "L/s2",
      "litres/second/second", 0.0, 0.0010),

  LITRES_PER_TONNE("litres/tonne", "L/t", Domain.VOLUME_PER_MASS, "L/t", "litres/tonne", 0.0, 1.0E-6),

  LITER_PER_UK_TON("liter per UK ton", "L/tonUK", Domain.VOLUME_PER_MASS, "L/tonUK", "liter per UK ton", 0.0,
      9.842064392690496E-7),

  MILLION_CUBIC_FEET("million cubic feet", "M(ft3)", Domain.VOLUME, "M(ft3)", "million cubic feet", 0.0, 28316.846592),

  MILLION_CUBIC_FEET_PER_ACRE_FOOT("million cubic feet per acre-foot", "M(ft3)/acre.ft", Domain.DIMENSIONLESS,
      "M(ft3)/acre.ft", "million cubic feet per acre-foot", 0.0, 22.95684113865932),

  MILLION_CUBIC_FEET_PER_DAY("million cubic feet per day", "M(ft3)/d", Domain.FLOWRATE, "M(ft3)/d",
      "million cubic feet per day", 0.0, 0.3277413194444444),

  MILLION_CUBIC_METERS("million cubic meters", "M(m3)", Domain.VOLUME, "M(m3)", "million cubic meters", 0.0, 1.0E-9),

  MILLION_CUBIC_METERS_PER_DAY("million cubic meters per day", "M(m3)/d", Domain.FLOWRATE, "M(m3)/d",
      "million cubic meters per day", 0.0, 11.574074074074074),

  MEGAAMPERE("megaampere", "MA", Domain.ELECTRIC_CURRENT, "MA", "megaampere", 0.0, 1000000.0),

  MEGABECQUEREL("megabecquerel", "MBq", Domain.RADIOACTIVITY, "MBq", "megabecquerel", 0.0, 1000000.0),

  MEGACOULOMB("megacoulomb", "MC", Domain.ELECTRIC_CHARGE, "MC", "megacoulomb", 0.0, 1000000.0),

  MEGAEUCLID("megaeuclid", "MEuc", Domain.DIMENSIONLESS, "MEuc", "mega euclid", 0.0, 1000000.0),

  MEGAFARAD("megafarad", "MF", Domain.CAPACITANCE, "MF", "megafarad", 0.0, 1000000.0),

  MEGAGRAY("megagray", "MGy", Domain.ABSORBED_DOSE, "MGy", "megagray", 0.0, 1000000.0),

  MEGAHENRY("megahenry", "MH", Domain.INDUCTANCE, "MH", "megahenry", 0.0, 1000000.0),

  MEGAHERTZ("megahertz", "MHz", Domain.ROTATIONAL_VELOCITY, "MHz", "megahertz", 0.0, 6283185.307),

  MEGAJOULES("megajoules", "MJ", Domain.ENERGY, "MJ", "megajoules", 0.0, 1000000.0),

  MEGAJOULES_PER_YEAR("megajoules/year", "MJ/a", Domain.POWER, "MJ/a", "megajoules/year", 0.0, 0.0316875355494539),

  MEGAJOULES_PER_KILOGRAM("megajoules/kilogram", "MJ/kg", Domain.SPECIFIC_ENERGY, "MJ/kg", "megajoules/kilogram", 0.0,
      1000000.0),

  MEGAJOULES_PER_METER("megajoules/meter", "MJ/m", Domain.FORCE, "MJ/m", "megajoules/meter", 0.0, 1000000.0),

  MEGAJOULES_PER_CUBIC_METER("megajoules/cubic meter", "MJ/m3", Domain.MODULUS_OF_COMPRESSION, "MJ/m3",
      "megajoules/cubic meter", 0.0, 1000000.0),

  MEGAJOULES_PER_MOLE_KILOGRAM("megajoules/mole (kilogram)", "MJ/mol(kg)", Domain.CHEMICAL_POTENTIAL, "MJ/mol(kg)",
      "megajoules/mole (kilogram)", 0.0, 1000000.0),

  METERS_PER_SECOND_PER_METER("meters/sec/meter", "m/s/m", Domain.VELOCITY_GRADIENT, "m/s/m", "meters/second/meter",
      0.0, 1.0, true),

  FEET_PER_SECOND_PER_FOOT("feet/sec/foot", "ft/s/ft", Domain.VELOCITY_GRADIENT, "ft/s/ft", "feet/second/foot", 0.0,
      1.0, true),

  MILLION_BARRELS("million barrels", "MMbbl", Domain.VOLUME, "MMbbl", "million barrels", 0.0, 158987.3),

  MILLION_BARRELS_PER_ACRE_FOOT("million barrels/acre foot", "MMbbl/acre.ft", Domain.DIMENSIONLESS, "MMbbl/acre.ft",
      "million barrels/acre foot", 0.0, 128.8923533164868),

  MILLION_STANDARD_CUBIC_FEET_AT_60_DEG_F("million standard cubic feet at 60 deg F", "MMscf(60F)",
      Domain.STANDARD_VOLUME, "MMscf(60F)", "million standard cubic feet at 60 deg F", 0.0, 28262.357),

  MILLION_STANDARD_CUBIC_FEET_PER_DAY("million standard cubic feet/day", "MMscf(60F)/d",
      Domain.STANDARD_VOLUME_PER_TIME, "MMscf(60F)/d", "million standard cubic feet/day", 0.0, 0.327110613425926),

  MILLION_STD_CU_FT_PER_STOCK_TANK_BARREL("million std cu ft/ stock tank barrel", "MMscf60/stb60",
      Domain.DIMENSIONLESS, "MMscf60/stb60", "million std cu ft/ stock tank barrel", 0.0, 1777648.717853565),

  MILLION_STANDARD_CUBIC_METERS_15C("million standard cubic meters 15C", "MMscm(15C)", Domain.STANDARD_VOLUME,
      "MMscm(15C)", "million standard cubic meters 15C", 0.0, 1000000.0),

  MILLION_STD_CUBIC_METERS_AT_15_DEGC_PER_DAY("million std cubic meters, 15 degC/day", "MMscm(15C)/d",
      Domain.STANDARD_VOLUME_PER_TIME, "MMscm(15C)/d", "million std cubic meters, 15 degC/day", 0.0, 11.57407407407407),

  MILLION_STOCK_TANK_BARRELS_60_DEG_F("million stock tank barrels 60 deg F", "MMstb(60F)", Domain.STANDARD_VOLUME,
      "MMstb(60F)", "million stock tank barrels 60 deg F", 0.0, 158987.3),

  MILLION_STBS_AT_60_DEG_F_PER_ACRE_FOOT("million stbs, 60 deg F/acre foot", "MMstb(60F)/acre.ft",
      Domain.STANDARD_VOLUME_PER_VOLUME, "MMstb(60F)/acre.ft", "million stbs, 60 deg F/acre foot", 0.0,
      128.8923533164868),

  MILLION_STOCK_TANK_BARRELS_AT_60_DEG_F_PER_DAY("million stock tank barrels, 60 deg F/day", "MMstb(60F)/d",
      Domain.STANDARD_VOLUME_PER_TIME, "MMstb(60F)/d", "million stock tank barrels, 60 deg F/day", 0.0,
      1.840130787037037),

  MILLION_STOCK_TANK_BARRELS_60_DEG_F_PER_ACRE("million stock tank barrels 60 deg F/acre", "MMstb/acre",
      Domain.STANDARD_VOLUME_PER_AREA, "MMstb/acre", "million stock tank barrels 60 deg F/acre", 0.0, 39.2864564813376),

  MEGANEWTONS("meganewtons", "MN", Domain.FORCE, "MN", "meganewtons", 0.0, 1000000.0),

  MEGAPOISE("megapoise", "MP", Domain.DYNAMIC_VISCOSITY, "MP", "megapoise", 0.0, 100000.0),

  MEGAPASCALS("megapascals", "MPa", Domain.PRESSURE, "MPa", "megapascals", 0.0, 1000000.0),

  MEGAPASCAL_SECONDS_PER_METER_MEGARAYL("megapascal seconds/meter (megarayl)", "MPa.s/m",
      Domain.MASS_PER_TIME_PER_AREA, "MPa.s/m", "megapascal seconds/meter (megarayl)", 0.0, 1000000.0),

  MEGAPASCAL_PER_HOUR("megapascal per hour", "MPa/h", Domain.PRESSURE_PER_TIME, "MPa/h", "megapascal per hour", 0.0,
      277.77777777777777),

  MEGAPASCAL_PER_METER("megapascal per meter", "MPa/m", Domain.FORCE_PER_VOLUME, "MPa/m", "megapascal per meter", 0.0,
      1000000.0),

  MEGAVOLT("megavolt", "MV", Domain.ELECTRIC_POTENTIAL, "MV", "megavolt", 0.0, 1000000.0),

  MEGAWATTS("megawatts", "MW", Domain.POWER, "MW", "megawatts", 0.0, 1000000.0),

  MEGAWATT_HOURS("megawatt hours", "MW.h", Domain.ENERGY, "MW.h", "megawatt hours", 0.0, 3.6E9),

  MEGAWATT_HOURS_PER_KILOGRAM("megawatt hours/kilogram", "MW.h/kg", Domain.SPECIFIC_ENERGY, "MW.h/kg",
      "megawatt hours/kilogram", 0.0, 3.6E9),

  MEGAWATT_HOURS_PER_CUBIC_METER("megawatt hours/cubic meter", "MW.h/m3", Domain.MODULUS_OF_COMPRESSION, "MW.h/m3",
      "megawatt hours/cubic meter", 0.0, 3.6E9),

  MEGAWEBER("megaweber", "MWb", Domain.MAGNETIC_FLUX, "MWb", "megaweber", 0.0, 1000000.0),

  MEGAYEARS("megayears", "MY", Domain.TIME, "MY", "megayears", 0.0, 3.155815E13),

  THOUSAND_BARRELS("thousand barrels", "Mbbl", Domain.VOLUME, "Mbbl", "thousand barrels", 0.0, 158.9873),

  THOUSAND_BARREL_FEET_PER_DAY("thousand barrel feet/day", "Mbbl.ft/d", Domain.VOLUME_LENGTH_PER_TIME, "Mbbl.ft/d",
      "thousand barrel feet/day", 0.0, 5.60871875E-4),

  THOUSAND_BARRELS_PER_DAY("thousand barrels/day", "Mbbl/d", Domain.FLOWRATE, "Mbbl/d", "thousand barrels/day", 0.0,
      0.001840130787037037),

  MEGABYTE("megabyte", "Mbyte", Domain.DIGITAL_STORAGE, "Mbyte", "megabyte", 0.0, 1048576.0),

  MEGACALORIE("megacalorie", "Mcal", Domain.ENERGY, "Mcal", "megacalorie", 0.0, 4184000.0),

  MILLIONS_OF_ELECTRON_VOLTS("millions of electron volts", "MeV", Domain.ENERGY, "MeV", "millions of electron volts",
      0.0, 1.60219E-13),

  MEGAFLOPS("megaflops", "Mflops", Domain.DIMENSIONLESS, "Mflops", "megaflops", 0.0, 1000000.0),

  MEGAGRAM("megagram", "Mg", Domain.MASS, "Mg", "megagram", 0.0, 1000.0),

  MEGAGRAMS_PER_YEAR("megagrams/year", "Mg/a", Domain.MASS_PER_TIME, "Mg/a", "megagrams/year", 0.0, 3.16875355494539E-5),

  MEGAGRAMS_PER_DAY("megagrams/day", "Mg/d", Domain.MASS_PER_TIME, "Mg/d", "megagrams/day", 0.0, 0.01157407407407407),

  MEGAGRAMS_PER_HOUR("megagrams/hour", "Mg/h", Domain.MASS_PER_TIME, "Mg/h", "megagrams/hour", 0.0, 0.277777777777778),

  THOUSAND_KILOGRAMS_PER_INCH("thousand kilograms per inch", "Mg/in", Domain.MASS_PER_LENGTH, "Mg/in",
      "thousand kilograms per inch", 0.0, 39370.078740157485),

  MEGAGRAMS_PER_SQUARE_METER("megagrams/square meter", "Mg/m2", Domain.MASS_PER_AREA, "Mg/m2",
      "megagrams/square meter", 0.0, 1000.0),

  THOUSAND_KILOGRAMS_PER_CUBIC_METER("thousand kilograms per cubic meter", "Mg/m3", Domain.MASS_PER_VOLUME, "Mg/m3",
      "thousand kilograms per cubic meter", 0.0, 1000000.0),

  MEGAGAMMA("megagamma", "Mgamma", Domain.MAGNETIC_FIELD_STRENGTH, "Mgamma", "megagamma", 0.0, 795.7747),

  MEGAGAUSS("megagauss", "Mgauss", Domain.MAGNETIC_FLUX_DENSITY, "Mgauss", "megagauss", 0.0, 100.0),

  THOUSAND_KILOGRAMS_FORCE("thousand kilograms force", "Mgf", Domain.FORCE, "Mgf", "thousand kilograms force", 0.0,
      4448222.0),

  MILLION_POUNDS_MASS_PER_YEAR("million pounds mass/year", "Mlbm/yr", Domain.MASS_PER_TIME, "Mlbm/yr",
      "million pounds mass/year", 0.0, 0.014373225299962135),

  MEGAMETER("megameter", "Mm", Domain.DISTANCE, "Mm", "megameter", 0.0, 1000000.0),

  MEGAMHO("megamho", "Mmho", Domain.ELECTRIC_CONDUCTANCE, "Mmho", "megamho", 0.0, 1000000.0),

  MEGAOHM("megaohm", "Mohm", Domain.ELECTRIC_RESISTANCE, "Mohm", "megaohm", 0.0, 1000000.0),

  MEGA_POUNDS_PER_SQUARE_INCH("mega pounds per square inch", "Mpsi", Domain.PRESSURE, "Mpsi",
      "mega pounds per square inch", 0.0, 6.894757E9),

  MEGARADIAN("megaradian", "Mrad", Domain.PLANE_ANGLE, "Mrad", "megaradian", 0.0, 1000000.0),

  MEGARAD("megarad", "Mrd", Domain.ABSORBED_DOSE, "Mrd", "megarad", 0.0, 10000.0),

  THOUSAND_CUBIC_FEET_AT_60_DEG_F("thousand cubic feet at 60 deg F", "Mscf(60F)", Domain.STANDARD_VOLUME, "Mscf(60F)",
      "thousand cubic feet at 60 deg F", 0.0, 28.262357),

  THOUSAND_STANDARD_CUBIC_FEET_PER_DAY("thousand standard cubic feet/day", "Mscf(60F)/d",
      Domain.STANDARD_VOLUME_PER_TIME, "Mscf(60F)/d", "thousand standard cubic feet/day", 0.0, 3.27110613425926E-4),

  THOUSAND_STD_CU_FT_PER_STOCK_TANK_BARREL("thousand std cu ft/ stock tank barrel", "Mscf60/stb60",
      Domain.DIMENSIONLESS, "Mscf60/stb60", "thousand std cu ft/ stock tank barrel", 0.0, 177.7648717853565),

  THOUSAND_STD_CUBIC_METERS_AT_15_DEGC_PER_DAY("thousand std cubic meters, 15 degC/day", "Mscm(15C)/d",
      Domain.STANDARD_VOLUME_PER_TIME, "Mscm(15C)/d", "thousand std cubic meters, 15 degC/day", 0.0,
      0.01157407407407407),

  MEGA_STANDARD_CUBIC_METERS_15C("mega standard cubic meters 15C", "Msm3", Domain.STANDARD_VOLUME, "Msm3",
      "mega standard cubic meters 15C", 0.0, 1000000.0),

  THOUSAND_STOCK_TANK_BARRELS_60_F("thousand stock tank barrels 60 F", "Mstb(60F)", Domain.STANDARD_VOLUME,
      "Mstb(60F)", "thousand stock tank barrels 60 F", 0.0, 158.9873),

  THOUSAND_STOCK_TANK_BARRELS_AT_60_DEG_F_PER_DAY("thousand stock tank barrels,60 deg F/day", "Mstb(60F)/d",
      Domain.STANDARD_VOLUME_PER_TIME, "Mstb(60F)/d", "thousand stock tank barrels,60 deg F/day", 0.0,
      0.001840130787037037),

  NEWTON("newton", "N", Domain.FORCE, "N", "newton", 0.0, 1.0),

  NEWTON_METER("newton meter", "N.m", Domain.ENERGY, "N.m", "newton meter", 0.0, 1.0),

  NEWTON_METERS_PER_METER("newton meters/meter", "N.m/m", Domain.FORCE, "N.m/m", "newton meters/meter", 0.0, 1.0),

  NEWTON_SQUARE_METERS("newton square meters", "N.m2", Domain.FORCE_AREA, "N.m2", "newton square meters", 0.0, 1.0),

  NEWTON_SECONDS_PER_METER_SQUARED("newton seconds/meter squared", "N.s/m2", Domain.DYNAMIC_VISCOSITY, "N.s/m2",
      "newton seconds/meter squared", 0.0, 1.0),

  NEWTON_PER_THIRTY_METERS("newton per thirty meters", "N/30m", Domain.FORCE_PER_LENGTH, "N/30m",
      "newton per thirty meters", 0.0, 0.03333333333333333),

  NEWTONS_PER_METER("newtons/meter", "N/m", Domain.FORCE_PER_LENGTH, "N/m", "newtons/meter", 0.0, 1.0),

  NEWTONS_PER_SQUARE_METER("newtons/square meter", "N/m2", Domain.PRESSURE, "N/m2", "newtons/square meter", 0.0, 1.0),

  NEWTONS_PER_CUBIC_METER("newtons/cubic meter", "N/m3", Domain.FORCE_PER_VOLUME, "N/m3", "newtons/cubic meter", 0.0,
      1.0),

  NEWTONS_PER_SQUARE_MILLIMETER("newtons/square millimeter", "N/mm2", Domain.PRESSURE, "N/mm2",
      "newtons/square millimeter", 0.0, 1000000.0),

  NEWTONS_FOURTH_METERS_PER_KILOGRAM("newtons fourth meters/kilogram", "N4/kg.m7", Domain.PARACHOR, "N4/kg.m7",
      "newtons fourth meters/kilogram", 0.0, 1.0),

  OCTAVE("octave", "O", Domain.FREQUENCY_INTERVAL, "O", "octave", 0.0, 1.0),

  OERSTED("oersted", "Oe", Domain.MAGNETIC_FIELD_STRENGTH, "Oe", "oersted", 0.0, 79.57747),

  POISE("poise", "P", Domain.DYNAMIC_VISCOSITY, "P", "poise", 0.0, 0.1),

  PASCAL("pascal", "Pa", Domain.PRESSURE, "Pa", "pascal", 0.0, 1.0),

  PASCAL_GAUGE("pascal gauge", "Pa(g)", Domain.PRESSURE, "Pa(g)", "pascal gauge", 101325.0, 1.0),

  PASCAL_SECONDS("pascal seconds", "Pa.s", Domain.DYNAMIC_VISCOSITY, "Pa.s", "pascal seconds", 0.0, 1.0),

  PASCAL_SECONDS_PER_CUBIC_METER("pascal seconds/cubic meter", "Pa.s/m3", Domain.PRESSURE_TIME_PER_VOLUME, "Pa.s/m3",
      "pascal seconds/cubic meter", 0.0, 1.0),

  PASCAL_SECOND_PER_CUBIC_METER_SQUARED("pascal second /cubic meter squared", "Pa.s/m6",
      Domain.NON_DARCY_FLOW_COEFFICIENT, "Pa.s/m6", "pascal second /cubic meter squared", 0.0, 1.0),

  PASCAL_SECONDS_SQUARED_PER_CUBIC_METER("pascal seconds squared/ cubic meter", "Pa.s2/m3",
      Domain.MASS_PER_VOLUME_PER_LENGTH, "Pa.s2/m3", "pascal seconds squared/ cubic meter", 0.0, 1.0),

  PASCAL_PER_HOUR("pascal per hour", "Pa/h", Domain.PRESSURE_PER_TIME, "Pa/h", "pascal per hour", 0.0,
      2.777777777777778E-4),

  PASCALS_PER_METER("pascals/meter", "Pa/m", Domain.PRESSURE_PER_LENGTH, "Pa/m", "pascals/meter", 0.0, 1.0),

  PASCALS_PER_CUBIC_METER("pascals/cubic meter", "Pa/m3", Domain.DARCY_FLOW_COEFFICIENT, "Pa/m3",
      "pascals/cubic meter", 0.0, 1.0),

  PASCAL_PER_SECOND("pascal/ second", "Pa/s", Domain.PRESSURE_PER_TIME, "Pa/s", "pascal/ second", 0.0, 1.0),

  PASCAL_SQUARED("pascal squared", "Pa2", Domain.PRESSURE_SQUARED, "Pa2", "pascal squared", 0.0, 1.0),

  SIEMENS("siemens", "S", Domain.ELECTRIC_CONDUCTANCE, "S", "siemens", 0.0, 1.0),

  SIEMENS_PER_SQUARE_METER("siemens per square meter", "S.m2", Domain.COUNTERION_CONDUCTIVITY, "S.m2",
      "counterion conductivity", 0.0, 1.0),

  SIEMENS_PER_METER("siemens/meter", "S/m", Domain.CONDUCTIVITY, "S/m", "siemens/meter", 0.0, 1.0),

  SIEVERT("sievert", "Sv", Domain.DOSE_EQUIVALENT, "Sv", "sievert", 0.0, 1.0),

  SIEVERTS_PER_HOUR("sieverts per hour", "Sv/h", Domain.DOSE_EQUIVALENT_RATE, "Sv/h", "sieverts per hour", 0.0,
      2.777777777777778E-4),

  SIEVERT_PER_SECOND("sievert per second", "Sv/s", Domain.DOSE_EQUIVALENT_RATE, "Sv/s", "sievert per second", 0.0, 1.0),

  TESLA("tesla", "T", Domain.MAGNETIC_FLUX_DENSITY, "T", "tesla", 0.0, 1.0),

  TESLA_PER_METER("tesla per meter", "T/m", Domain.MAGNETIC_FLUX_DENSITY_PER_LENGTH, "T/m", "tesla per meter", 0.0, 1.0),

  TERAAMP("teraamp", "TA", Domain.ELECTRIC_CURRENT, "TA", "teraamp", 0.0, 1.0E12),

  TERABECQUEREL("terabecquerel", "TBq", Domain.RADIOACTIVITY, "TBq", "terabecquerel", 0.0, 1.0E12),

  TERACOULOMB("teracoulomb", "TC", Domain.ELECTRIC_CHARGE, "TC", "teracoulomb", 0.0, 1.0E12),

  TERAEUCLID("teraeuclid", "TEuc", Domain.DIMENSIONLESS, "TEuc", "tera euclid", 0.0, 1.0E12),

  TERAFARAD("terafarad", "TF", Domain.CAPACITANCE, "TF", "terafarad", 0.0, 1.0E12),

  TERAGRAY("teragray", "TGy", Domain.ABSORBED_DOSE, "TGy", "teragray", 0.0, 1.0E12),

  TERAHENRY("terahenry", "TH", Domain.INDUCTANCE, "TH", "terahenry", 0.0, 1.0E12),

  TERAHERTZ("terahertz", "THz", Domain.ROTATIONAL_VELOCITY, "THz", "terahertz", 0.0, 6.283185307E12),

  TERAJOULES("terajoules", "TJ", Domain.ENERGY, "TJ", "terajoules", 0.0, 1.0E12),

  TERAJOULES_PER_YEAR("terajoules/year", "TJ/a", Domain.POWER, "TJ/a", "terajoules/year", 0.0, 31687.5355494539),

  TERANEWTON("teranewton", "TN", Domain.FORCE, "TN", "teranewton", 0.0, 1.0E12),

  TERAPOISE("terapoise", "TP", Domain.DYNAMIC_VISCOSITY, "TP", "terapoise", 0.0, 1.0E11),

  TERAPASCAL("terapascal", "TPa", Domain.PRESSURE, "TPa", "terapascal", 0.0, 1.0E12),

  TERASIEMEN("terasiemen", "TS", Domain.ELECTRIC_CONDUCTANCE, "TS", "terasiemen", 0.0, 1.0E12),

  TERATESLA("teratesla", "TT", Domain.MAGNETIC_FLUX_DENSITY, "TT", "teratesla", 0.0, 1.0E12),

  TERAVOLT("teravolt", "TV", Domain.ELECTRIC_POTENTIAL, "TV", "teravolt", 0.0, 1.0E12),

  TERAWATTS("terawatts", "TW", Domain.POWER, "TW", "terawatts", 0.0, 1.0E12),

  TERRAWATT_HOURS("terrawatt hours", "TW.h", Domain.ENERGY, "TW.h", "terrawatt hours", 0.0, 3.6E15),

  TERAWEBER("teraweber", "TWb", Domain.MAGNETIC_FLUX, "TWb", "teraweber", 0.0, 1.0E12),

  TERAYEAR("terayear", "Ta", Domain.TIME, "Ta", "terayear", 0.0, 3.155815E19),

  TERACALORIE("teracalorie", "Tcal", Domain.ENERGY, "Tcal", "teracalorie", 0.0, 4.184E12),

  TERA_ELECTRON_VOLTS("tera electron volts", "TeV", Domain.ENERGY, "TeV", "tera electron volts", 0.0, 1.602177E-7),

  TERAGRAM("teragram", "Tg", Domain.MASS, "Tg", "teragram", 0.0, 1.0E9),

  TERAGAMMA("teragamma", "Tgamma", Domain.MAGNETIC_FIELD_STRENGTH, "Tgamma", "teragamma", 0.0, 7.957747E8),

  TERAGAUSS("teragauss", "Tgauss", Domain.MAGNETIC_FLUX_DENSITY, "Tgauss", "teragauss", 0.0, 1.0E8),

  TERAMETER("terameter", "Tm", Domain.DISTANCE, "Tm", "terameter", 0.0, 1.0E12),

  TERAMHO("teramho", "Tmho", Domain.ELECTRIC_CONDUCTANCE, "Tmho", "teramho", 0.0, 1.0E12),

  TERAOHM("teraohm", "Tohm", Domain.ELECTRIC_RESISTANCE, "Tohm", "teraohm", 0.0, 1.0E12),

  TERARAD("terarad", "Trd", Domain.ABSORBED_DOSE, "Trd", "terarad", 0.0, 1.0E10),

  VOLT("volt", "V", Domain.ELECTRIC_POTENTIAL, "V", "volt", 0.0, 1.0),

  VOLTS_PER_BEL("volts/Bel", "V/B", Domain.POTENTIAL_DIFFERENCE_PER_POWER_DROP, "V/B", "volts/Bel", 0.0, 1.0),

  VOLTS_PER_DECIBEL("volts/decibel", "V/dB", Domain.POTENTIAL_DIFFERENCE_PER_POWER_DROP, "V/dB", "volts/decibel", 0.0,
      10.0),

  VOLTS_PER_METER("volts/meter", "V/m", Domain.ELECTRIC_POTENTIAL_PER_LENGTH, "V/m", "volts/meter", 0.0, 1.0),

  WATT("watt", "W", Domain.POWER, "W", "watt", 0.0, 1.0),

  WATTS_PER_DELTA_KELVIN("Watts per delta kelvin", "W/K", Domain.THERMAL_CONDUCTANCE, "W/K", "Watts per delta kelvin",
      0.0, 1.0),

  WATTS_PER_WATT("watts/watt", "W/W", Domain.DIMENSIONLESS, "W/W", "watts/watt", 0.0, 1.0),

  WATTS_PER_SQUARE_CENTIMETER("watts/square centimeter", "W/cm2", Domain.POWER_PER_AREA, "W/cm2",
      "watts/square centimeter", 0.0, 10000.0),

  WATTS_PER_KILOWATT("watts/kilowatt", "W/kW", Domain.DIMENSIONLESS, "W/kW", "watts/kilowatt", 0.0, 0.0010),

  WATTS_PER_METER_KELVIN("watts/meter kelvin", "W/m.K", Domain.THERMAL_CONDUCTIVITY, "W/m.K", "watts/meter kelvin",
      0.0, 1.0),

  WATTS_PER_SQUARE_METER("watts/square meter", "W/m2", Domain.POWER_PER_AREA, "W/m2", "watts/square meter", 0.0, 1.0),

  WATTS_PER_SQUARE_METER_KELVIN("watts/square meter kelvin", "W/m2.K", Domain.HEAT_TRANSFER_COEFFICIENT, "W/m2.K",
      "watts/square meter kelvin", 0.0, 1.0),

  WATTS_PER_SQUARE_METER_STERADIAN("watts/square meter steradian", "W/m2.sr", Domain.WATTS_PER_SQUARE_METER_STERADIAN,
      "W/m2.sr", "watts/square meter steradian", 0.0, 1.0),

  WATTS_PER_CUBIC_METER("watts/cubic meter", "W/m3", Domain.POWER_PER_VOLUME, "W/m3", "watts/cubic meter", 0.0, 1.0),

  WATTS_PER_CUBIC_METER_KELVIN("watts/cubic meter kelvin", "W/m3.K", Domain.WATTS_PER_CUBIC_METER_KELVIN, "W/m3.K",
      "watts/cubic meter kelvin", 0.0, 1.0),

  WATTS_PER_SQUARE_MILLIMETER("watts per square millimeter", "W/mm2", Domain.POWER_PER_AREA, "W/mm2",
      "watts per square millimeter", 0.0, 1000000.0),

  WATTS_PER_STERADIAN("watts/steradian", "W/sr", Domain.WATTS_PER_STERADIAN, "W/sr", "watts/steradian", 0.0, 1.0),

  WEBER("weber", "Wb", Domain.MAGNETIC_FLUX, "Wb", "weber", 0.0, 1.0),

  WEBER_METERS("weber meters", "Wb.m", Domain.WEBER_METERS, "Wb.m", "weber meters", 0.0, 1.0),

  WEBERS_PER_METER("webers/meter", "Wb/m", Domain.MAGNETIC_VECTOR_POTENTIAL, "Wb/m", "webers/meter", 0.0, 1.0),

  WEBERS_PER_MILLIMETER("webers/millimeter", "Wb/mm", Domain.MAGNETIC_VECTOR_POTENTIAL, "Wb/mm", "webers/millimeter",
      0.0, 1000.0),

  ANNUM("annum", "a", Domain.TIME, "a", "annum", 0.0, 3.155815E7),

  ATTOJOULE("attojoule", "aJ", Domain.ENERGY, "aJ", "attojoule", 0.0, 1.0E-18),

  ACRE("acre", "acre", Domain.AREA, "acre", "acre", 0.0, 4046.873),

  ACRE_FOOT("acre foot", "acre.ft", Domain.VOLUME, "acre.ft", "acre foot", 0.0, 1233.489),

  ACRE_FEET_PER_MILLION_STBS_AT_60_DEG_F("acre feet/million stbs, 60 deg F", "acre.ft/MMstb",
      Domain.VOLUME_PER_STANDARD_VOLUME, "acre.ft/MMstb", "acre feet/million stbs, 60 deg F", 0.0, 0.00775841214990128),

  ATTOGRAM("attogram", "ag", Domain.MASS, "ag", "attogram", 0.0, 1.0E-21),

  ANGSTROM("Angstrom", "angstrom", Domain.DISTANCE, "angstrom", "Angstrom", 0.0, 1.0E-10),

  TECHNICAL_ATMOSPHERE("Technical atmosphere", "at", Domain.PRESSURE, "at", "Technical atmosphere", 0.0, 98066.5),

  ATMOSPHERE("Atmosphere", "atm", Domain.PRESSURE, "atm", "Atmosphere", 0.0, 101325.0),

  ATMOSPHERES_PER_FT("Atmospheres per ft", "atm/ft", Domain.FORCE_PER_VOLUME, "atm/ft", "Atmospheres per ft", 0.0,
      332431.1023622047),

  ATMOSPHERE_PER_HOUR("atmosphere per hour", "atm/h", Domain.PRESSURE_PER_TIME, "atm/h", "atmosphere per hour", 0.0,
      28.145833333333332),

  ATMOSPHERES_PER_HUNDRED_METER("Atmospheres per hundred meter", "atm/hm", Domain.FORCE_PER_VOLUME, "atm/hm",
      "Atmospheres per hundred meter", 0.0, 1013.25),

  ATMOSPHERES_PER_METER("Atmospheres/meter", "atm/m", Domain.PRESSURE_PER_LENGTH, "atm/m", "Atmospheres/meter", 0.0,
      101325.0),

  BARN("barn", "b", Domain.AREA, "b", "barn", 0.0, 1.0E-28),

  BARNS_PER_CUBIC_CENTIMETER("barns/cubic centimeter", "b/cm3", Domain.INVERSE_LENGTH, "b/cm3",
      "barns/cubic centimeter", 0.0, 1.0E-22),

  BARNS_PER_ELECTRON("barns/electron", "b/elec", Domain.CROSS_SECTION_ABSORPTION, "b/elec", "barns/electron", 0.0,
      6.023E-5),

  BAR("bar", "bar", Domain.PRESSURE, "bar", "bar", 0.0, 100000.0),

  BAR_PER_HOUR("bar per hour", "bar/h", Domain.PRESSURE_PER_TIME, "bar/h", "bar per hour", 0.0, 27.77777777777778),

  BAR_PER_KILOMETER("bar per kilometer", "bar/km", Domain.FORCE_PER_VOLUME, "bar/km", "bar per kilometer", 0.0, 100.0),

  BAR_PER_METER("bar per meter", "bar/m", Domain.FORCE_PER_VOLUME, "bar/m", "bar per meter", 0.0, 100000.0),

  BAR_SQUARED("bar squared", "bar2", Domain.PRESSURE_SQUARED, "bar2", "bar squared", 0.0, 1.0E10),

  BAR_SQUARED_PER_CENTIPOISE("bar squared per centipoise", "bar2/cP", Domain.PRESSURE_PER_TIME, "bar2/cP",
      "bar squared per centipoise", 0.0, 1.0E13),

  BARREL("barrel", "bbl", Domain.VOLUME, "bbl", "barrel", 0.0, 0.1589873),

  BARREL_PER_HUNDRED_BARREL("barrel per hundred barrel", "bbl/100bbl", Domain.DIMENSIONLESS, "bbl/100bbl",
      "barrel per hundred barrel", 0.0, 0.01),

  BARREL_PER_MILLION_CUBIC_FEET("barrel per million cubic feet", "bbl/M(ft3)", Domain.DIMENSIONLESS, "bbl/M(ft3)",
      "barrel per million cubic feet", 0.0, 5.614582836720892E-6),

  BARRELS_PER_MILLION_STD_CUBIC_FEET_AT_60_DEGF("barrels/million std cubic feet, 60 degF", "bbl/MMscf(60F)",
      Domain.VOLUME_PER_STANDARD_VOLUME, "bbl/MMscf(60F)", "barrels/million std cubic feet, 60 degF", 0.0,
      5.62540838331354E-6),

  BARRELS_PER_ACRE("barrels/acre", "bbl/acre", Domain.DISTANCE, "bbl/acre", "barrels/acre", 0.0, 3.92863982342936E-5),

  BARREL_PER_ACRE_FOOT("barrel/acre foot", "bbl/acre.ft", Domain.DIMENSIONLESS, "bbl/acre.ft", "barrel/acre foot", 0.0,
      1.288923533164868E-4),

  BARREL_PER_BARREL("barrel/barrel", "bbl/bbl", Domain.DIMENSIONLESS, "bbl/bbl", "barrel/barrel", 0.0, 1.0),

  BARRELS_PER_CENTIPOISE_DAY_PSI("barrels/centiPoise day psi", "bbl/cP.d.psi", Domain.VOLUME_PER_PASCAL_SECOND_SQUARED,
      "bbl/cP.d.psi", "barrels/centiPoise day psi", 0.0, 2.66888419978278E-7),

  BARREL_PER_DAY("barrel/day", "bbl/d", Domain.FLOWRATE, "bbl/d", "barrel/day", 0.0, 1.840130787037037E-6),

  BARRELS_PER_DAY_ACRE_FOOT("barrels/day acre foot", "bbl/d.acre.ft", Domain.FREQUENCY, "bbl/d.acre.ft",
      "barrels/day acre foot", 0.0, 1.491809639267566E-9),

  BARRELS_PER_DAY_FOOT("barrels/day foot", "bbl/d.ft", Domain.AREA_PER_TIME, "bbl/d.ft", "barrels/day foot", 0.0,
      6.03717449815301E-6),

  BARRELS_PER_DAY_FOOT_POUNDS_PER_SQ_IN("barrels/day foot pounds/sq in", "bbl/d.ft.psi", Domain.MOBILITY,
      "bbl/d.ft.psi", "barrels/day foot pounds/sq in", 0.0, 8.75618169291339E-10),

  BARREL_PER_DAY_POUNDS_PER_SQUARE_INCH("barrel/day pounds/square inch", "bbl/d.psi",
      Domain.VOLUME_PER_TIME_PER_PRESSURE, "bbl/d.psi", "barrel/day pounds/square inch", 0.0, 2.66888418E-10),

  BARRELS_PER_DAY_PER_DAY("barrels/day per day", "bbl/d2", Domain.VOLUME_PER_TIME_PER_TIME, "bbl/d2",
      "barrels/day per day", 0.0, 2.12978E-11),

  BARREL_PER_FOOT("barrel/foot", "bbl/ft", Domain.AREA, "bbl/ft", "barrel/foot", 0.0, 0.52161187664042),

  BARREL_PER_CUBIC_FOOT("barrel per cubic foot", "bbl/ft3", Domain.DIMENSIONLESS, "bbl/ft3", "barrel per cubic foot",
      0.0, 5.614582836720892),

  BARREL_PER_HOUR("barrel/hour", "bbl/hr", Domain.FLOWRATE, "bbl/hr", "barrel/hour", 0.0, 4.41631388888889E-5),

  BARRELS_PER_HOUR_PER_HOUR("barrels/hour/hour", "bbl/hr2", Domain.VOLUME_PER_TIME_PER_TIME, "bbl/hr2",
      "barrels/hour/hour", 0.0, 1.226753858024691E-8),

  BARREL_PER_INCH("barrel/inch", "bbl/in", Domain.AREA, "bbl/in", "barrel/inch", 0.0, 6.25934251968504),

  BARREL_PER_THOUSAND_CUBIC_FEET("barrel per thousand cubic feet", "bbl/k(ft3)", Domain.DIMENSIONLESS, "bbl/k(ft3)",
      "barrel per thousand cubic feet", 0.0, 0.005614582836720892),

  BARREL_PER_DAY_PER_KILOPASCAL("barrel per day per kilopascal", "bbl/kPa.d", Domain.VOLUME_PER_TIME_PER_PRESSURE,
      "bbl/kPa.d", "barrel per day per kilopascal", 0.0, 1.840130787037037E-9),

  BARREL_PER_MILE("barrel/mile", "bbl/mi", Domain.AREA, "bbl/mi", "barrel/mile", 0.0, 9.87901281515947E-5),

  BARREL_PER_MINUTE("barrel per minute", "bbl/min", Domain.FLOWRATE, "bbl/min", "barrel per minute", 0.0,
      0.0026497883333333333),

  BARREL_PER_DAY_PER_PSI("barrel per day per psi", "bbl/psi.d", Domain.VOLUME_PER_TIME_PER_PRESSURE, "bbl/psi.d",
      "barrel per day per psi", 0.0, 2.668884178277838E-10),

  BARRELS_PER_STOCK_TANK_BARREL_AT_60_DEG_F("barrels/stock tank barrel, 60 deg F", "bbl/stb(60F)",
      Domain.VOLUME_PER_STANDARD_VOLUME, "bbl/stb(60F)", "barrels/stock tank barrel, 60 deg F", 0.0, 1.0),

  BARREL_PER_UK_TON("barrel per U.K. ton", "bbl/tonUK", Domain.VOLUME_PER_MASS, "bbl/tonUK", "barrel per U.K. ton",
      0.0, 156.4763),

  BARREL_PER_US_TON("barrel per U.S. ton", "bbl/tonUS", Domain.VOLUME_PER_MASS, "bbl/tonUS", "barrel per U.S. ton",
      0.0, 175.2535),

  BILLION_CUBIC_FEET("billion cubic feet", "bcf", Domain.VOLUME, "bcf", "billion cubic feet", 0.0, 2.831685E7),

  BRAKE_HORSEPOWER("brake-horsepower", "bhp", Domain.POWER, "bhp", "break-horsepower", 0.0, 745.6999),

  BIT("bit", "bit", Domain.DIGITAL_STORAGE, "bit", "bit", 0.0, 0.125),

  BYTE("byte", "byte", Domain.DIGITAL_STORAGE, "byte", "byte", 0.0, 1.0),

  BYTES_PER_SECOND("bytes per second", "byte/s", Domain.DIGITAL_STORAGE_PER_TIME, "byte/s", "bytes per second", 0.0,
      1.0),

  CYCLE("cycle", "c", Domain.PLANE_ANGLE, "c", "cycle", 0.0, 6.28318530717959),

  CYCLES_PER_SECOND("cycles/second", "c/s", Domain.ROTATIONAL_VELOCITY, "c/s", "cycles/second", 0.0, 6.28318530717959),

  CENTIAMP("centiamp", "cA", Domain.ELECTRIC_CURRENT, "cA", "centiamp", 0.0, 0.01),

  CENTICOULOMB("centicoulomb", "cC", Domain.ELECTRIC_CHARGE, "cC", "centicoulomb", 0.0, 0.01),

  CENTIEUCLID("centiEuclid", "cEuc", Domain.DIMENSIONLESS, "cEuc", "centiEuclid", 0.0, 0.01),

  CENTIFARAD("centifarad", "cF", Domain.CAPACITANCE, "cF", "centifarad", 0.0, 0.01),

  CENTIGRAY("centigray", "cGy", Domain.ABSORBED_DOSE, "cGy", "centigray", 0.0, 0.01),

  CENTIHENRY("centihenry", "cH", Domain.INDUCTANCE, "cH", "centihenry", 0.0, 0.01),

  CENTIHERTZ("centihertz", "cHz", Domain.ROTATIONAL_VELOCITY, "cHz", "centihertz", 0.0, 0.06283185307),

  CENTIJOULE("centijoule", "cJ", Domain.ENERGY, "cJ", "centijoule", 0.0, 0.01),

  CENTINEWTON("centinewton", "cN", Domain.FORCE, "cN", "centinewton", 0.0, 0.01),

  CENTIPOISE("centipoise", "cP", Domain.DYNAMIC_VISCOSITY, "cP", "centipoise", 0.0, 0.0010),

  CENTIPASCAL("centipascal", "cPa", Domain.PRESSURE, "cPa", "centipascal", 0.0, 0.01),

  CENTISIEMEN("centisiemen", "cS", Domain.ELECTRIC_CONDUCTANCE, "cS", "centisiemen", 0.0, 0.01),

  CENTISTOKE("centiStoke", "cSt", Domain.AREA_PER_TIME, "cSt", "centiStoke", 0.0, 1.0E-6),

  CENTITESLA("centitesla", "cT", Domain.MAGNETIC_FLUX_DENSITY, "cT", "centitesla", 0.0, 0.01),

  CENTIVOLT("centivolt", "cV", Domain.ELECTRIC_POTENTIAL, "cV", "centivolt", 0.0, 0.01),

  CENTIWATT("centiwatt", "cW", Domain.POWER, "cW", "centiwatt", 0.0, 0.01),

  CENTIWEBER("centiweber", "cWb", Domain.MAGNETIC_FLUX, "cWb", "centiweber", 0.0, 0.01),

  CENTIYEAR("centiyear", "ca", Domain.TIME, "ca", "centiyear", 0.0, 3.155815E-5),

  CALORIE("calorie", "cal", Domain.ENERGY, "cal", "calorie", 0.0, 4.184),

  CALORIES_PER_CUBIC_CENTIMETER("calories/cubic centimeter", "cal/cm3", Domain.MODULUS_OF_COMPRESSION, "cal/cm3",
      "calories/cubic centimeter", 0.0, 4184000.0),

  CALORIES_PER_GRAM("calories/gram", "cal/g", Domain.SPECIFIC_ENERGY, "cal/g", "calories/gram", 0.0, 4184.0),

  CALORIES_PER_GRAM_DEGREE_KELVIN("calories/gram degree Kelvin", "cal/g.K", Domain.SPECIFIC_HEAT_CAPACITY, "cal/g.K",
      "calories/gram degree Kelvin", 0.0, 4184.0),

  CALORIES_PER_HOUR("calories/hour", "cal/h", Domain.POWER, "cal/h", "calories/hour", 0.0, 0.001162222),

  CALORIES_PER_HOUR_CENTIMETER_DEGREE_CELSIUS("calories/hour centimeter degree Celsius", "cal/h.cm.degC",
      Domain.THERMAL_CONDUCTIVITY, "cal/h.cm.degC", "calories/hour centimeter degree Celsius", 0.0, 0.1162222),

  CALORIES_PER_HOUR_CENTIMETER_SQUARED("calories/hour centimeter squared", "cal/h.cm2", Domain.POWER_PER_AREA,
      "cal/h.cm2", "calories/hour centimeter squared", 0.0, 11.62222),

  CALORIES_PER_HOUR_SQUARE_CENTIMETER_DEG_C("calories/hour square centimeter deg C", "cal/h.cm2.degC",
      Domain.HEAT_TRANSFER_COEFFICIENT, "cal/h.cm2.degC", "calories/hour square centimeter deg C", 0.0, 11.62222),

  CALORIES_PER_HOUR_CUBIC_CENTIMETER("calories/hour cubic centimeter", "cal/h.cm3", Domain.POWER_PER_VOLUME,
      "cal/h.cm3", "calories/hour cubic centimeter", 0.0, 1162.222),

  CALORIES_PER_KILOGRAM("calories/kilogram", "cal/kg", Domain.SPECIFIC_ENERGY, "cal/kg", "calories/kilogram", 0.0,
      4.184),

  CALORIES_PER_POUND_MASS("calories/pound mass", "cal/lbm", Domain.SPECIFIC_ENERGY, "cal/lbm", "calories/pound mass",
      0.0, 9.224141),

  CALORIES_PER_MILLILITER("calories/milliliter", "cal/mL", Domain.MODULUS_OF_COMPRESSION, "cal/mL",
      "calories/milliliter", 0.0, 4184000.0),

  CALORIES_PER_CUBIC_MILLIMETER("calories/cubic millimeter", "cal/mm3", Domain.MODULUS_OF_COMPRESSION, "cal/mm3",
      "calories/cubic millimeter", 0.0, 4.184E9),

  CALORIES_PER_GRAM_MOL_DEGREE_CELSIUS("calories/gram mol degree celsius", "cal/mol(g).degC",
      Domain.MOLAR_HEAT_CAPACITY, "cal/mol(g).degC", "calories/gram mol degree celsius", 0.0, 4184.0),

  CALORIES_PER_SECOND_CENTIMETER_DEG_C("calories/second centimeter deg C", "cal/s.cm.degC",
      Domain.THERMAL_CONDUCTIVITY, "cal/s.cm.degC", "calories/second centimeter deg C", 0.0, 418.4),

  CALORIES_PER_SECOND_SQUARE_CENTIMETER_DEG_C("calories/second square centimeter deg C", "cal/s.cm2.degC",
      Domain.HEAT_TRANSFER_COEFFICIENT, "cal/s.cm2.degC", "calories/second square centimeter deg C", 0.0, 41840.0),

  CALORIES_PER_SECOND_CUBIC_CENTIMETER("calories/second cubic centimeter", "cal/s.cm3", Domain.POWER_PER_VOLUME,
      "cal/s.cm3", "calories/second cubic centimeter", 0.0, 4184000.0),

  CENTICALORIE("centicalorie", "ccal", Domain.ENERGY, "ccal", "centicalorie", 0.0, 0.04184),

  CENTESIMAL_SECOND("centesimal second", "ccgr", Domain.PLANE_ANGLE, "ccgr", "centesimal second", 0.0,
      1.570796326794897E-6),

  CANDELA("candela", "cd", Domain.LUMINOUS_INTENSITY, "cd", "candela", 0.0, 1.0),

  CANDELAS_PER_SQUARE_METER("candelas/square meter", "cd/m2", Domain.CANDELAS_PER_SQUARE_METER, "cd/m2",
      "candelas/square meter", 0.0, 1.0),

  CENTIELECTRON_VOLTS("centielectron-volts", "ceV", Domain.ENERGY, "ceV", "centielectron-volts", 0.0, 1.60219E-21),

  CENTIGRAM("centigram", "cg", Domain.MASS, "cg", "centigram", 0.0, 1.0E-5),

  CENTIGAMMA("centigamma", "cgamma", Domain.MAGNETIC_FIELD_STRENGTH, "cgamma", "centigamma", 0.0, 7.957747E-6),

  CENTIGAUSS("centigauss", "cgauss", Domain.MAGNETIC_FLUX_DENSITY, "cgauss", "centigauss", 0.0, 1.0E-6),

  CENTESIMAL_MINUTE("centesimal minute", "cgr", Domain.PLANE_ANGLE, "cgr", "centesimal minute", 0.0,
      1.570796326794897E-4),

  CH("ch", "ch", Domain.POWER, "ch", "ch", 0.0, 735.499),

  CH_HOURS("ch hours", "ch.h", Domain.ENERGY, "ch.h", "ch hours", 0.0, 2647796.0),

  CHAINS("chains", "chBnA", Domain.DISTANCE, "chBnA", "chains", 0.0, 20.1167824),

  CENTIMETER("centimeter", "cm", Domain.DISTANCE, "cm", "centimeter", 0.0, 0.01),

  CENTIMETER_PER_YEAR("centimeter per year", "cm/a", Domain.VELOCITY, "cm/a", "centimeter per year", 0.0,
      3.168753554945395E-10),

  CENTIMETER_PER_SECOND("centimeter/second", "cm/s", Domain.VELOCITY, "cm/s", "centimeter/second", 0.0, 0.01),

  CENTIMETER_PER_SECOND_SQUARED("centimeter/second squared", "cm/s2", Domain.ACCELERATION, "cm/s2",
      "centimeter/second squared", 0.0, 0.01),

  SQUARE_CENTIMETER("square centimeter", "cm2", Domain.AREA, "cm2", "square centimeter", 0.0, 1.0E-4),

  CENTIMETERS_SQUARED_PER_GRAM("centimeters squared/gram", "cm2/g", Domain.MASS_ATTENUATION_COEFFICIENT, "cm2/g",
      "centimeters squared/gram", 0.0, 0.1),

  CENTIMETERS_SQUARED_PER_SECOND("centimeters squared/second", "cm2/s", Domain.AREA_PER_TIME, "cm2/s",
      "centimeters squared/second", 0.0, 1.0E-4),

  CUBIC_CENTIMETER("cubic centimeter", "cm3", Domain.VOLUME, "cm3", "cubic centimeter", 0.0, 1.0E-6),

  CUBIC_CENTIMETER_PER_THIRTY_MINUTES("cubic centimeter per thirty minutes", "cm3/30min", Domain.FLOWRATE, "cm3/30min",
      "cubic centimeter per thirty minutes", 0.0, 5.555555555555555E-10),

  CUBIC_CENTIMETERS_PER_CUBIC_CENTIMETERS("cubic centimeters/ cubic centimeters", "cm3/cm3", Domain.DIMENSIONLESS,
      "cm3/cm3", "cubic centimeters/ cubic centimeters", 0.0, 1.0),

  CUBIC_CENTIMETERS_PER_GRAM("cubic centimeters/gram", "cm3/g", Domain.VOLUME_PER_MASS, "cm3/g",
      "cubic centimeters/gram", 0.0, 0.0010),

  CUBIC_CENTIMETER_PER_HOUR("cubic centimeter per hour", "cm3/h", Domain.FLOWRATE, "cm3/h",
      "cubic centimeter per hour", 0.0, 2.7777777777777777E-10),

  CUBIC_CENTIMETER_PER_CUBIC_METER("cubic centimeter/cubic meter", "cm3/m3", Domain.DIMENSIONLESS, "cm3/m3",
      "cubic centimeter/cubic meter", 0.0, 1.0E-6),

  CUBIC_CENTIMETER_PER_MINUTE("cubic centimeter per minute", "cm3/min", Domain.FLOWRATE, "cm3/min",
      "cubic centimeter per minute", 0.0, 1.6666666666666667E-8),

  CUBIC_CENTIMETER_PER_SECOND("cubic centimeter per second", "cm3/s", Domain.FLOWRATE, "cm3/s",
      "cubic centimeter per second", 0.0, 1.0E-6),

  CENTIMETERS_FOURTH("centimeters fourth", "cm4", Domain.MOMENT_OF_SECTION, "cm4", "centimeters fourth", 0.0, 1.0E-8),

  CM_OF_WATER_AT_4_DEGC("cm of water at 4 degC.", "cmH2O(4degC)", Domain.PRESSURE, "cmH2O(4degC)",
      "cm of water at 4 degC.", 0.0, 98.0638),

  CENTIMHO("centimho", "cmho", Domain.ELECTRIC_CONDUCTANCE, "cmho", "centimho", 0.0, 0.01),

  CENTIOHM("centiohm", "cohm", Domain.ELECTRIC_RESISTANCE, "cohm", "centiohm", 0.0, 0.01),

  CENTIRAD("centirad", "crd", Domain.ABSORBED_DOSE, "crd", "centirad", 0.0, 1.0E-4),

  TEN_MILLI_SECOND("ten milli second", "cs", Domain.TIME, "cs", "ten milli second", 0.0, 0.01),

  CARAT("carat", "ct", Domain.MASS, "ct", "carat", 0.0, 2.0E-4),

  CAPTURE_UNIT("capture unit", "cu", Domain.INVERSE_LENGTH, "cu", "capture unit", 0.0, 0.1),

  CUBIC_FEET("cubic feet", "cu ft", Domain.VOLUME, "cu ft", "cubic feet", 0.0, 0.02831685),

  CUBIC_INCH("cubic inch", "cu in", Domain.VOLUME, "cu in", "cubic inch", 0.0, 1.638706E-5),

  CUBIC_YARD("cubic yard", "cu yd", Domain.VOLUME, "yd3", "cubic yard", 0.0, 0.7645549),

  CUBEM("cubem", "cubem", Domain.VOLUME, "cubem", "cubem", 0.0, 4.168182E9),

  UK_HUNDREDWEIGHT("UK hundredweight", "cwtUK", Domain.MASS, "cwtUK", "UK hundredweight", 0.0, 50.80235),

  US_HUNDREDWEIGHT("US hundredweight", "cwtUS", Domain.MASS, "cwtUS", "US hundredweight", 0.0, 45.35924),

  DAY("day", "d", Domain.TIME, "d", "day", 0.0, 86400.0),

  DAY_PER_BARREL("day per barrel", "d/bbl", Domain.TIME_PER_VOLUME, "d/bbl", "day per barrel", 0.0, 543439.633228566),

  DAYS_PER_CUBIC_FOOT("days/cubic foot", "d/ft3", Domain.TIME_PER_VOLUME, "d/ft3", "days/cubic foot", 0.0,
      3051186.837519),

  DAY_PER_THOUSAND_CUBIC_FEET("day per thousand cubic feet", "d/k(ft3)", Domain.TIME_PER_VOLUME, "d/k(ft3)",
      "day per thousand cubic feet", 0.0, 3051.187204736614),

  DAYS_PER_CUBIC_METER("days/cubic meter", "d/m3", Domain.TIME_PER_VOLUME, "d/m3", "days/cubic meter", 0.0, 86400.0),

  DECIAMP("deciamp", "dA", Domain.ELECTRIC_CURRENT, "dA", "deciamp", 0.0, 0.1),

  API_GRAVITY("API gravity", "dAPI", Domain.API_OIL_GRAVITY, "dAPI", "API gravity", 0.0, 1.0),

  DECIBEL("decibel", "dB", Domain.LEVEL_OF_POWER_INTENSITY, "dB", "decibel", 0.0, 0.1),

  DECIBEL_WATT("decibel watt", "dB.W", Domain.NORMALIZED_POWER, "dB.W", "decibel watt", 0.0, 1.0),

  DECIBELS_PER_OCTAVE("decibels/octave", "dB/O", Domain.ATTENUATION_PER_OCTAVE, "dB/O", "decibels/octave", 0.0, 1.0),

  DECIBELS_PER_FOOT("decibels/foot", "dB/ft", Domain.ATTENUATION_PER_LENGTH, "dB/ft", "decibels/foot", 0.0,
      3.28083989501312),

  DECIBELS_PER_KILOMETER("decibels/kilometer", "dB/km", Domain.ATTENUATION_PER_LENGTH, "dB/km", "decibels/kilometer",
      0.0, 1.0E-4),

  DECIBELS_PER_METER("decibels/meter", "dB/m", Domain.ATTENUATION_PER_LENGTH, "dB/m", "decibels/meter", 0.0, 1.0),

  DECICOULOMB("decicoulomb", "dC", Domain.ELECTRIC_CHARGE, "dC", "decicoulomb", 0.0, 0.1),

  DECIEUCLID("decieuclid", "dEuc", Domain.DIMENSIONLESS, "dEuc", "deci euclid", 0.0, 0.1),

  DECIFARAD("decifarad", "dF", Domain.CAPACITANCE, "dF", "decifarad", 0.0, 0.1),

  DECIGRAY("decigray", "dGy", Domain.ABSORBED_DOSE, "dGy", "decigray", 0.0, 0.1),

  DECIHENRY("decihenry", "dH", Domain.INDUCTANCE, "dH", "decihenry", 0.0, 0.1),

  DECIHERTZ("decihertz", "dHz", Domain.ROTATIONAL_VELOCITY, "dHz", "decihertz", 0.0, 0.6283185307),

  DECIJOULE("decijoule", "dJ", Domain.ENERGY, "dJ", "decijoule", 0.0, 0.1),

  DECINEWTON("decinewton", "dN", Domain.FORCE, "dN", "decinewton", 0.0, 0.1),

  DECINEWTON_METERS("decinewton meters", "dN.m", Domain.ENERGY, "dN.m", "decinewton meters", 0.0, 0.1),

  DECIPOISE("decipoise", "dP", Domain.DYNAMIC_VISCOSITY, "dP", "decipoise", 0.0, 0.01),

  DECIPASCAL("decipascal", "dPa", Domain.PRESSURE, "dPa", "decipascal", 0.0, 0.1),

  DECISIEMEN("decisiemen", "dS", Domain.ELECTRIC_CONDUCTANCE, "dS", "decisiemen", 0.0, 0.1),

  DECITESLA("decitesla", "dT", Domain.MAGNETIC_FLUX_DENSITY, "dT", "decitesla", 0.0, 0.1),

  DECIVOLT("decivolt", "dV", Domain.ELECTRIC_POTENTIAL, "dV", "decivolt", 0.0, 0.1),

  DECIWATT("deciwatt", "dW", Domain.POWER, "dW", "deciwatt", 0.0, 0.1),

  DECIWEBER("deciweber", "dWb", Domain.MAGNETIC_FLUX, "dWb", "deciweber", 0.0, 0.1),

  DECANEWTONS("decanewtons", "daN", Domain.FORCE, "daN", "decanewtons", 0.0, 10.0),

  DECANEWTON_METERS("decanewton meters", "daN.m", Domain.ENERGY, "daN.m", "decanewton meters", 0.0, 10.0),

  DECANEWTON("decanewton", "dcN", Domain.FORCE, "dcN", "decanewton", 0.0, 10.0),

  DECICALORIE("decicalorie", "dcal", Domain.ENERGY, "dcal", "decicalorie", 0.0, 0.4184),

  CHANGE_IN_DEGREES_CELSIUS("change in degrees Celsius", "ddegC", Domain.TEMPERATURE, "ddegC",
      "change in degrees Celsius", 0.0, 1.0),

  CHANGE_IN_DEGREES_FAHRENHEIT("change in degrees Fahrenheit", "ddegF", Domain.TEMPERATURE, "ddegF",
      "change in degrees Fahrenheit", 0.0, 0.555555555555556),

  CHANGE_IN_DEGREES_KELVIN("change in degrees Kelvin", "ddegK", Domain.TEMPERATURE, "ddegK",
      "change in degrees Kelvin", 0.0, 1.0),

  CHANGE_IN_DEGREES_RANKINE("change in degrees Rankine", "ddegR", Domain.TEMPERATURE, "ddegR",
      "change in degrees Rankine", 0.0, 0.555555555555556),

  DECIELECTRON_VOLTS("decielectron-volts", "deV", Domain.ENERGY, "deV", "decielectron-volts", 0.0, 1.60219E-20),

  DEGREES_CELSIUS("degrees Celsius", "degC", Domain.TEMPERATURE, "degC", "degrees Celsius", 273.15, 1.0),

  DEGREES_C_SQUARE_METERS_HOURS_PER_KILOCAL("degrees C square meters hours/kilocal", "degC.m2.h/kcal",
      Domain.THERMAL_INSULANCE, "degC.m2.h/kcal", "degrees C square meters hours/kilocal", 0.0, 0.8604208),

  DEGREES_CELSIUS_PER_HUNDRED_METER("degrees Celsius per hundred meter", "degC/100m", Domain.TEMPERATURE_PER_LENGTH,
      "degC/100m", "degrees Celsius per hundred meter", 0.0, 0.01),

  DEGREES_CELSIUS_PER_FOOT("degrees Celsius per foot", "degC/ft", Domain.TEMPERATURE_PER_LENGTH, "degC/ft",
      "degrees Celsius per foot", 0.0, 3.280839895013123),

  DEGREES_CELSIUS_PER_HOUR("degrees Celsius per hour", "degC/h", Domain.TEMPERATURE_PER_TIME, "degC/h",
      "degrees Celsius per hour", 0.0, 2.777777777777778E-4),

  DEGREES_CELSIUS_PER_KILOMETER("degrees Celsius/kilometer", "degC/km", Domain.TEMPERATURE_PER_LENGTH, "degC/km",
      "degrees Celsius/kilometer", 0.0, 0.0010),

  DEGREES_CELSIUS_PER_METER("degrees Celsius/meter", "degC/m", Domain.TEMPERATURE_PER_LENGTH, "degC/m",
      "degrees Celsius/meter", 0.0, 1.0),

  DEGREES_CELSIUS_PER_MINUTE("degrees Celsius per minute", "degC/min", Domain.TEMPERATURE_PER_TIME, "degC/min",
      "degrees Celsius per minute", 0.0, 0.016666666666666666),

  DEGREES_CELSIUS_PER_SECOND("degrees Celsius per second", "degC/s", Domain.TEMPERATURE_PER_TIME, "degC/s",
      "degrees Celsius per second", 0.0, 1.0),

  DEGREE_FAHRENHEIT("degree Fahrenheit", "degF", Domain.TEMPERATURE, "degF", "degree Fahrenheit", 255.372222222222,
      0.555555555555556),

  DEGREES_F_SQUARE_FEET_HOURS_PER_BTU("degrees F square feet hours/Btu", "degF.ft2.h/Btu", Domain.THERMAL_INSULANCE,
      "degF.ft2.h/Btu", "degrees F square feet hours/Btu", 0.0, 0.1761102),

  DEGREES_FAHRENHEIT_PER_100_FEET("degrees Fahrenheit/100 feet.", "degF/100ft", Domain.TEMPERATURE_PER_LENGTH,
      "degF/100ft", "degrees Fahrenheit/100 feet.", 0.0, 0.01822689),

  DEGREES_FAHRENHEIT_PER_FOOT("degrees Fahrenheit/foot", "degF/ft", Domain.TEMPERATURE_PER_LENGTH, "degF/ft",
      "degrees Fahrenheit/foot", 0.0, 1.82268883056285),

  DEGREES_FAHRENHEIT_PER_HOUR("degrees Fahrenheit per hour", "degF/h", Domain.TEMPERATURE_PER_TIME, "degF/h",
      "degrees Fahrenheit per hour", 0.0, 1.5432098765432098E-4),

  DEGREES_FAHRENHEIT_PER_METER("degrees Fahrenheit per meter", "degF/m", Domain.TEMPERATURE_PER_LENGTH, "degF/m",
      "degrees Fahrenheit per meter", 0.0, 0.5555555555555556),

  DEGREES_FAHRENHEIT_PER_MINUTE("degrees Fahrenheit per minute", "degF/min", Domain.TEMPERATURE_PER_TIME, "degF/min",
      "degrees Fahrenheit per minute", 0.0, 0.009259259259259259),

  DEGREES_FAHRENHEIT_PER_SECOND("degrees Fahrenheit per second", "degF/s", Domain.TEMPERATURE_PER_TIME, "degF/s",
      "degrees Fahrenheit per second", 0.0, 0.5555555555555556),

  DEGREES_RANKINE("degrees Rankine", "degR", Domain.TEMPERATURE, "degR", "degrees Rankine", 0.0, 0.555555555555556),

  DEGREE_OF_AN_ANGLE("degree of an angle", "dega", Domain.PLANE_ANGLE, "dega", "degree of an angle", 0.0,
      0.0174532925199433),

  DEGREES_OF_AN_ANGLE_PER_100_FEET("degrees of an angle/100 feet", "dega/100ft", Domain.ANGLE_PER_LENGTH, "dega/100ft",
      "degrees of an angle/100 feet", 0.0, 5.72614501312336E-4),

  DEGREES_OF_AN_ANGLE_PER_THIRTY_FEET("degrees of an angle per thirty feet", "dega/30ft", Domain.ANGLE_PER_LENGTH,
      "dega/30ft", "degrees of an angle per thirty feet", 0.0, 0.0019087150043744531),

  DEGREES_OF_AN_ANGLE_PER_30_METERS("degrees of an angle/30 meters", "dega/30m", Domain.ANGLE_PER_LENGTH, "dega/30m",
      "degrees of an angle/30 meters", 0.0, 5.817763333333334E-4),

  DEGREES_OF_AN_ANGLE_PER_FOOT("degrees of an angle/foot", "dega/ft", Domain.ANGLE_PER_LENGTH, "dega/ft",
      "degrees of an angle/foot", 0.0, 0.0572614501312336),

  DEGREES_OF_AN_ANGLE_PER_HOUR("degrees of an angle per hour", "dega/h", Domain.ROTATIONAL_VELOCITY, "dega/h",
      "degrees of an angle per hour", 0.0, 4.848136111111111E-6),

  DEGREES_OF_AN_ANGLE_PER_METER("degrees of an angle/meter", "dega/m", Domain.ANGLE_PER_LENGTH, "dega/m",
      "degrees of an angle/meter", 0.0, 0.01745329),

  DEGREES_OF_AN_ANGLE_PER_MINUTE("degrees of an angle/minute", "dega/min", Domain.ROTATIONAL_VELOCITY, "dega/min",
      "degrees of an angle/minute", 0.0, 2.90888166666667E-4),

  DEGREES_OF_AN_ANGLE_PER_SECOND("degrees of an angle per second", "dega/s", Domain.ROTATIONAL_VELOCITY, "dega/s",
      "degrees of an angle per second", 0.0, 0.01745329),

  DECIGAMMA("decigamma", "dgamma", Domain.MAGNETIC_FIELD_STRENGTH, "dgamma", "decigamma", 0.0, 7.957747E-5),

  DECIGAUSS("decigauss", "dgauss", Domain.MAGNETIC_FLUX_DENSITY, "dgauss", "decigauss", 0.0, 1.0E-5),

  DEKANEWTON("dekanewton", "dkN", Domain.FORCE, "dkN", "dekanewton", 0.0, 10.0),

  DEKAMETER("dekameter", "dkm", Domain.DISTANCE, "dkm", "dekameter", 0.0, 10.0),

  DECIMETER("decimeter", "dm", Domain.DISTANCE, "dm", "decimeter", 0.0, 0.1),

  DECIMETER_PER_SECOND("decimeter per second", "dm/s", Domain.VELOCITY, "dm/s", "decimeter per second", 0.0, 0.1),

  CUBIC_DECIMETER("cubic decimeter", "dm3", Domain.VOLUME, "dm3", "cubic decimeter", 0.0, 0.0010),

  CUBIC_DECIMETERS_PER_100_KILOMETERS("cubic decimeters/100 kilometers", "dm3/100km", Domain.AREA, "dm3/100km",
      "cubic decimeters/100 kilometers", 0.0, 1.0E-8),

  CUBIC_DECIMETERS_PER_MEGAJOULE("cubic decimeters/megajoule", "dm3/MJ", Domain.ISOTHERMAL_COMPRESSIBILITY, "dm3/MJ",
      "cubic decimeters/megajoule", 0.0, 1.0E-9),

  CUBIC_DECIMETERS_PER_KILOWATT_HOUR("cubic decimeters/kilowatt hour", "dm3/kW.h", Domain.ISOTHERMAL_COMPRESSIBILITY,
      "dm3/kW.h", "cubic decimeters/kilowatt hour", 0.0, 2.777778E-10),

  CUBIC_DECIMETERS_PER_KILOGRAM("cubic decimeters/kilogram", "dm3/kg", Domain.VOLUME_PER_MASS, "dm3/kg",
      "cubic decimeters/kilogram", 0.0, 0.0010),

  CUBIC_DECIMETERS_PER_METER("cubic decimeters/meter", "dm3/m", Domain.AREA, "dm3/m", "cubic decimeters/meter", 0.0,
      0.0010),

  CUBIC_DECIMETERS_PER_CUBIC_METER("cubic decimeters/cubic meter", "dm3/m3", Domain.DIMENSIONLESS, "dm3/m3",
      "cubic decimeters/cubic meter", 0.0, 0.0010),

  CUBIC_DECIMETERS_PER_KILOGRAM_MOLE("cubic decimeters/kilogram mole", "dm3/mol(kg)", Domain.MOLAR_VOLUME,
      "dm3/mol(kg)", "cubic decimeters/kilogram mole", 0.0, 0.0010),

  CUBIC_DECIMETERS_PER_SECOND("cubic decimeters/second", "dm3/s", Domain.FLOWRATE, "dm3/s", "cubic decimeters/second",
      0.0, 0.0010),

  CUBIC_DECIMETERS_PER_SECOND_PER_SECOND("cubic decimeters/second/second", "dm3/s2", Domain.VOLUME_PER_TIME_PER_TIME,
      "dm3/s2", "cubic decimeters/second/second", 0.0, 0.0010),

  CUBIC_DECIMETERS_PER_TON("cubic decimeters/ton", "dm3/t", Domain.VOLUME_PER_MASS, "dm3/t", "cubic decimeters/ton",
      0.0, 1.0E-6),

  DECIMHO("decimho", "dmho", Domain.ELECTRIC_CONDUCTANCE, "dmho", "decimho", 0.0, 0.1),

  DECIOHM("deciohm", "dohm", Domain.ELECTRIC_RESISTANCE, "dohm", "deciohm", 0.0, 0.1),

  DECIRAD("decirad", "drd", Domain.ABSORBED_DOSE, "drd", "decirad", 0.0, 0.0010),

  DECISECOND("decisecond", "ds", Domain.TIME, "ds", "decisecond", 0.0, 0.1),

  DYNES("dynes", "dyne", Domain.FORCE, "dyne", "dynes", 0.0, 1.0E-5),

  DYNE_CENTIMETER_SQUARED("dyne centimeter squared", "dyne.cm2", Domain.FORCE_AREA, "dyne.cm2",
      "dyne centimeter squared", 0.0, 1.0E-9),

  DYNE_SECONDS_PER_SQUARE_CENTIMETER("dyne seconds/square centimeter", "dyne.s/cm2", Domain.DYNAMIC_VISCOSITY,
      "dyne.s/cm2", "dyne seconds/square centimeter", 0.0, 0.1),

  DYNES_PER_CENTIMETER("dynes/centimeter", "dyne/cm", Domain.FORCE_PER_LENGTH, "dyne/cm", "dynes/centimeter", 0.0,
      0.0010),

  DYNES_PER_SQUARE_CENTIMETER("dynes/square centimeter", "dyne/cm2", Domain.PRESSURE, "dyne/cm2",
      "dynes/square centimeter", 0.0, 0.1),

  ELECTRON_VOLTS("electron volts", "eV", Domain.ENERGY, "eV", "electron volts", 0.0, 1.60219E-19),

  ELECTRIC_HORSEPOWER("electric horsepower", "ehp", Domain.POWER, "ehp", "electric horsepower", 0.0, 746.0),

  EQUIVALENT("equivalent", "eq", Domain.ELECTROCHEMICAL_EQUIVALENT, "eq", "equivalent", 0.0, 1.0),

  EQUIVALENTS_PER_LITER("equivalents/ Liter", "eq/L", Domain.EQUIVALENT_PER_VOLUME, "eq/L", "equivalents/ Liter", 0.0,
      1000.0),

  EQUIVALENTS_PER_KILOGRAM("equivalents/ kilogram", "eq/kg", Domain.PER_MASS, "eq/kg", "equivalents/ kilogram", 0.0,
      1.0),

  EQUIVALENT_PER_CUBIC_METER("equivalent/ cubic meter", "eq/m3", Domain.EQUIVALENT_PER_VOLUME, "eq/m3",
      "equivalent/ cubic meter", 0.0, 1.0),

  ERGS("ergs", "erg", Domain.ENERGY, "erg", "ergs", 0.0, 1.0E-7),

  ERGS_PER_YEAR("ergs/year", "erg/a", Domain.POWER, "erg/a", "ergs/year", 0.0, 3.16875355494539E-15),

  ERGS_PER_SQUARE_CENTIMETER("ergs/square centimeter", "erg/cm2", Domain.FORCE_PER_LENGTH, "erg/cm2",
      "ergs/square centimeter", 0.0, 0.0010),

  ERGS_PER_CUBIC_CENTIMETER("ergs/cubic centimeter", "erg/cm3", Domain.MODULUS_OF_COMPRESSION, "erg/cm3",
      "ergs/cubic centimeter", 0.0, 0.1),

  ERGS_PER_GRAM("ergs/gram", "erg/g", Domain.SPECIFIC_ENERGY, "erg/g", "ergs/gram", 0.0, 1.0E-4),

  ERGS_PER_KILOGRAM("ergs/kilogram", "erg/kg", Domain.SPECIFIC_ENERGY, "erg/kg", "ergs/kilogram", 0.0, 1.0E-7),

  ERGS_PER_CUBIC_METER("ergs/cubic meter", "erg/m3", Domain.MODULUS_OF_COMPRESSION, "erg/m3", "ergs/cubic meter", 0.0,
      1.0E-7),

  FEMTOAMP("femtoamp", "fA", Domain.ELECTRIC_CURRENT, "fA", "femtoamp", 0.0, 1.0E-15),

  FEMTOCOULOMB("femtocoulomb", "fC", Domain.ELECTRIC_CHARGE, "fC", "femtocoulomb", 0.0, 1.0E-15),

  FEMTOEUCLID("femtoEuclid", "fEuc", Domain.DIMENSIONLESS, "fEuc", "femto Euclid", 0.0, 1.0E-15),

  FEMTOFARAD("femtofarad", "fF", Domain.CAPACITANCE, "fF", "femtofarad", 0.0, 1.0E-15),

  FEMTOGRAY("femtogray", "fGy", Domain.ABSORBED_DOSE, "fGy", "femtogray", 0.0, 1.0E-15),

  FEMTOHENRY("femtohenry", "fH", Domain.INDUCTANCE, "fH", "femtohenry", 0.0, 1.0E-15),

  FEMTOHERTZ("femtohertz", "fHz", Domain.ROTATIONAL_VELOCITY, "fHz", "femtohertz", 0.0, 6.283185307E-15),

  FEMTOJOULE("femtojoule", "fJ", Domain.ENERGY, "fJ", "femtojoule", 0.0, 1.0E-15),

  FEMTONEWTON("femtonewton", "fN", Domain.FORCE, "fN", "femtonewton", 0.0, 1.0E-15),

  FEMTOPOISE("femtopoise", "fP", Domain.DYNAMIC_VISCOSITY, "fP", "femtopoise", 0.0, 1.0E-16),

  FEMTOPASCAL("femtopascal", "fPa", Domain.PRESSURE, "fPa", "femtopascal", 0.0, 1.0E-15),

  FEMTOSIEMEN("femtosiemen", "fS", Domain.ELECTRIC_CONDUCTANCE, "fS", "femtosiemen", 0.0, 1.0E-15),

  FEMTOTESLA("femtotesla", "fT", Domain.MAGNETIC_FLUX_DENSITY, "fT", "femtotesla", 0.0, 1.0E-15),

  FEMTOVOLT("femtovolt", "fV", Domain.ELECTRIC_POTENTIAL, "fV", "femtovolt", 0.0, 1.0E-15),

  FEMTOWATT("femtowatt", "fW", Domain.POWER, "fW", "femtowatt", 0.0, 1.0E-15),

  FEMTOWEBER("femtoweber", "fWb", Domain.MAGNETIC_FLUX, "fWb", "femtoweber", 0.0, 1.0E-15),

  FEMTOYEAR("femtoyear", "fa", Domain.TIME, "fa", "femtoyear", 0.0, 3.155815E-8),

  FATHOMS("fathoms", "fathom", Domain.DISTANCE, "fathom", "fathoms", 0.0, 1.8288),

  FEMTOCALORIE("femtocalorie", "fcal", Domain.ENERGY, "fcal", "femtocalorie", 0.0, 4.184E-15),

  FEMTOELECTRON_VOLTS("femtoelectron-volts", "feV", Domain.ENERGY, "feV", "femtoelectron-volts", 0.0, 1.60219E-34),

  FEMTOGRAM("femtogram", "fg", Domain.MASS, "fg", "femtogram", 0.0, 1.0E-18),

  FEMTOGAMMA("femtogamma", "fgamma", Domain.MAGNETIC_FIELD_STRENGTH, "fgamma", "femtogamma", 0.0, 7.957747E-19),

  FEMTOGAUSS("femtogauss", "fgauss", Domain.MAGNETIC_FLUX_DENSITY, "fgauss", "femtogauss", 0.0, 1.0E-19),

  UK_FLID_OUNCE("UK flid ounce", "fl ozUK", Domain.VOLUME, "fl ozUK", "UK flid ounce", 0.0, 2.841308E-5),

  US_FLID_OUNCES("US flid ounces", "fl ozUS", Domain.VOLUME, "fl ozUS", "US flid ounces", 0.0, 2.957353E-5),

  FLOPS("flops", "flops", Domain.DIMENSIONLESS, "flops", "flops", 0.0, 1.0),

  UK_FLUID_OUNCE("UK fluid ounce", "flozUK", Domain.VOLUME, "flozUK", "UK fluid ounce", 0.0, 2.841308E-5),

  US_FLUID_OUNCES("US fluid ounces", "flozUS", Domain.VOLUME, "flozUS", "US fluid ounces", 0.0, 2.957353E-5),

  FEMTOMETER("femtometer", "fm", Domain.DISTANCE, "fm", "femtometer", 0.0, 1.0E-15),

  FEMTOMHO("femtomho", "fmho", Domain.ELECTRIC_CONDUCTANCE, "fmho", "femtomho", 0.0, 1.0E-15),

  FEMTOOHM("femtoohm", "fohm", Domain.ELECTRIC_RESISTANCE, "fohm", "femtoohm", 0.0, 1.0E-15),

  FOOTCANDLES("footcandles", "footcandle", Domain.ILLUMINANCE, "footcandle", "footcandles", 0.0, 10.76391),

  FOOTCANDLE_SECONDS("footcandle seconds", "footcandle.s", Domain.LIGHT_EXPOSURE, "footcandle.s", "footcandle seconds",
      0.0, 10.76391),

  FEMTORAD("femtorad", "frd", Domain.ABSORBED_DOSE, "frd", "femtorad", 0.0, 1.0E-17),

  FOOT("international foot", "ft", Domain.DISTANCE, "ft", "foot", 0.0, 0.3048, true),

  FOOT_POUNDS_FORCE("foot pounds force", "ft.lbf", Domain.ENERGY, "ft.lbf", "foot pounds force", 0.0, 1.355818),

  FOOT_POUNDS_FORCE_PER_BARREL("foot pounds force/barrel", "ft.lbf/bbl", Domain.MODULUS_OF_COMPRESSION, "ft.lbf/bbl",
      "foot pounds force/barrel", 0.0, 8.5278383870913),

  FOOT_POUNDS_FORCE_PER_US_GALLON("foot pounds force/US gallon", "ft.lbf/galUS", Domain.MODULUS_OF_COMPRESSION,
      "ft.lbf/galUS", "foot pounds force/US gallon", 0.0, 358.1692),

  FOOT_POUNDS_FORCE_PER_POUND_MASS("foot pounds force/pound mass", "ft.lbf/lbm", Domain.SPECIFIC_ENERGY, "ft.lbf/lbm",
      "foot pounds force/pound mass", 0.0, 2.98906683621683),

  FOOT_POUNDS_FORCE_PER_MINUTE("foot pounds force/minute", "ft.lbf/min", Domain.POWER, "ft.lbf/min",
      "foot pounds force/minute", 0.0, 0.02259697),

  FOOT_POUNDS_FORCE_PER_SECOND("foot pounds force/second", "ft.lbf/s", Domain.POWER, "ft.lbf/s",
      "foot pounds force/second", 0.0, 1.355818),

  FOOT_POUND_MASS("foot-pound mass", "ft.lbm", Domain.MASS_LENGTH, "ft.lbm", "foot-pound mass", 0.0, 0.1382549),

  FEET_PER_100_FEET("feet per 100 feet", "ft/100ft", Domain.DIMENSIONLESS, "ft/100ft", "feet per 100 feet", 0.0, 0.01),

  FEET_PER_BARREL("feet/barrel", "ft/bbl", Domain.INVERSE_AREA, "ft/bbl", "feet/barrel", 0.0, 1.917134),

  FEET_PER_DAY("feet/day", "ft/d", Domain.VELOCITY, "ft/d", "feet/day", 0.0, 3.52777777777778E-6),

  FEET_PER_DEGREE_FAHRENHEIT("feet/degree Fahrenheit", "ft/degF", Domain.LENGTH_PER_TEMPERATURE, "ft/degF",
      "feet/degree Fahrenheit", 0.0, 0.54864),

  FEET_PER_FEET("feet per feet", "ft/ft", Domain.DIMENSIONLESS, "ft/ft", "feet per feet", 0.0, 1.0),

  FEET_PER_CUBIC_FOOT("feet/cubic foot", "ft/ft3", Domain.INVERSE_AREA, "ft/ft3", "feet/cubic foot", 0.0, 10.76391),

  FEET_PER_US_GALLON("feet/US gallon", "ft/galUS", Domain.INVERSE_AREA, "ft/galUS", "feet/US gallon", 0.0, 80.51964),

  FEET_PER_HOUR("feet/hour", "ft/h", Domain.VELOCITY, "ft/h", "feet/hour", 0.0, 8.46666666666667E-5),

  FEET_PER_INCH("feet/inch", "ft/in", Domain.DIMENSIONLESS, "ft/in", "feet/inch", 0.0, 12.0),

  FEET_PER_METER("feet/meter", "ft/m", Domain.DIMENSIONLESS, "ft/m", "feet/meter", 0.0, 0.3048),

  FEET_PER_MILE("feet/mile", "ft/mi", Domain.DIMENSIONLESS, "ft/mi", "feet/mile", 0.0, 1.893939393939394E-4),

  FEET_PER_MINUTE("feet/minute", "ft/min", Domain.VELOCITY, "ft/min", "feet/minute", 0.0, 0.00508),

  FOOT_PER_MILLISECOND("foot per millisecond", "ft/ms", Domain.VELOCITY, "ft/ms", "foot per millisecond", 0.0, 304.8),

  FEET_PER_SECOND("feet/second", "ft/s", Domain.VELOCITY, "ft/s", "feet/second", 0.0, 0.3048, true),

  FEET_PER_SECOND_SQUARED("feet/second squared", "ft/s2", Domain.ACCELERATION, "ft/s2", "feet/second squared", 0.0,
      0.3048),

  FOOT_PER_MICROSECOND("foot per microsecond", "ft/us", Domain.VELOCITY, "ft/us", "foot per microsecond", 0.0, 304800.0),

  SQUARE_FOOT("square foot", "ft2", Domain.AREA, "ft2", "square foot", 0.0, 0.09290304),

  SQUARE_FEET_PER_HOUR("square feet/hour", "ft2/h", Domain.AREA_PER_TIME, "ft2/h", "square feet/hour", 0.0, 2.58064E-5),

  SQUARE_FEET_PER_CUBIC_INCH("square feet/cubic inch", "ft2/in3", Domain.INVERSE_LENGTH, "ft2/in3",
      "square feet/cubic inch", 0.0, 5669.291),

  SQUARE_FEET_PER_POUND("square feet/pound", "ft2/lbm", Domain.MASS_ATTENUATION_COEFFICIENT, "ft2/lbm",
      "square feet/pound", 0.0, 0.20481613),

  SQUARE_FEET_PER_SECOND("square feet/second", "ft2/s", Domain.AREA_PER_TIME, "ft2/s", "square feet/second", 0.0,
      0.09290304),

  CUBIC_FEET_AT_STANDARD_CONDITIONS("cubic feet at standard conditions", "ft3(std,60F)", Domain.AMOUNT_OF_SUBSTANCE,
      "ft3(std,60F)", "cubic feet at standard conditions", 0.0, 0.0011953),

  CUBIC_FEET_PER_BARREL("cubic feet/barrel", "ft3/bbl", Domain.DIMENSIONLESS, "ft3/bbl", "cubic feet/barrel", 0.0,
      0.1781076224327352),

  CUBIC_FEET_PER_DAY("cubic feet/day", "ft3/d", Domain.FLOWRATE, "ft3/d", "cubic feet/day", 0.0, 3.27741319444444E-7),

  CUBIC_FEET_PER_DAY_PER_FOOT("cubic feet per day per foot", "ft3/d.ft", Domain.AREA_PER_TIME, "ft3/d.ft",
      "cubic feet per day per foot", 0.0, 1.0752666666666667E-6),

  CUBIC_FEET_PER_DAY_FOOT_PSI("cubic feet/day foot psi", "ft3/d.ft.psi", Domain.MOBILITY, "ft3/d.ft.psi",
      "cubic feet/day foot psi", 0.0, 1.559542513689259E-10),

  CUBIC_FEET_PER_DAY_PER_DAY("cubic feet/day/day", "ft3/d2", Domain.VOLUME_PER_TIME_PER_TIME, "ft3/d2",
      "cubic feet/day/day", 0.0, 3.79330230838477E-12),

  CUBIC_FEET_PER_FOOT("cubic feet/foot", "ft3/ft", Domain.AREA, "ft3/ft", "cubic feet/foot", 0.0, 0.09290304),

  CUBIC_FEET_PER_CUBIC_FOOT("cubic feet/cubic foot", "ft3/ft3", Domain.DIMENSIONLESS, "ft3/ft3",
      "cubic feet/cubic foot", 0.0, 1.0),

  CUBIC_FEET_PER_HOUR("cubic feet/hour", "ft3/h", Domain.FLOWRATE, "ft3/h", "cubic feet/hour", 0.0, 7.86579166666667E-6),

  CUBIC_FEET_PER_HOUR_PER_HOUR("cubic feet/hour/hour", "ft3/h2", Domain.VOLUME_PER_TIME_PER_TIME, "ft3/h2",
      "cubic feet/hour/hour", 0.0, 2.18494212962963E-9),

  CUBIC_FEET_PER_KILOGRAM("cubic feet per kilogram", "ft3/kg", Domain.VOLUME_PER_MASS, "ft3/kg",
      "cubic feet per kilogram", 0.0, 0.02831685),

  CUBIC_FEET_PER_POUND_MASS("cubic feet/pound mass", "ft3/lbm", Domain.VOLUME_PER_MASS, "ft3/lbm",
      "cubic feet/pound mass", 0.0, 0.06242796),

  CUBIC_FEET_PER_MINUTE("cubic feet/minute", "ft3/min", Domain.FLOWRATE, "ft3/min", "cubic feet/minute", 0.0,
      4.719475E-4),

  CUBIC_FEET_PER_MIN_SQUARE_FOOT("cubic feet/min square foot", "ft3/min.ft2", Domain.VELOCITY, "ft3/min.ft2",
      "cubic feet/min square foot", 0.0, 0.00508),

  CUBIC_FEET_PER_MINUTE_PER_MINUTE("cubic feet/minute/minute", "ft3/min2", Domain.VOLUME_PER_TIME_PER_TIME, "ft3/min2",
      "cubic feet/minute/minute", 0.0, 7.86579166666667E-6),

  CUBIC_FEET_PER_MOLE_POUND_MASS("cubic feet/mole (pound mass)", "ft3/mol(lbm)", Domain.MOLAR_VOLUME, "ft3/mol(lbm)",
      "cubic feet/mole (pound mass)", 0.0, 0.06242796),

  CUBIC_FEET_PER_SECOND("cubic feet/second", "ft3/s", Domain.FLOWRATE, "ft3/s", "cubic feet/second", 0.0, 0.02831685),

  CUBIC_FEET_PER_SECOND_SQUARE_FOOT("cubic feet/second square foot", "ft3/s.ft2", Domain.VELOCITY, "ft3/s.ft2",
      "cubic feet/second square foot", 0.0, 0.3048),

  CUBIC_FEET_PER_SECOND_PER_SECOND("cubic feet/second/second", "ft3/s2", Domain.VOLUME_PER_TIME_PER_TIME, "ft3/s2",
      "cubic feet/second/second", 0.0, 0.02831685),

  CUBIC_FEET_PER_94_POUND_SACK("cubic feet per 94 pound sack", "ft3/sack94", Domain.VOLUME_PER_MASS, "ft3/sack94",
      "cubic feet per 94 pound sack", 0.0, 6.641272076418775E-4),

  CUBIC_FEET_PER_STD_CUBIC_FOOT_AT_60_DEG_F("cubic feet/std cubic foot, 60 deg F", "ft3/scf(60F)",
      Domain.VOLUME_PER_STANDARD_VOLUME, "ft3/scf(60F)", "cubic feet/std cubic foot, 60 deg F", 0.0, 1.0),

  BRITISH_FOOT_BENOIT_1895_A("British Foot (Benoit 1895 A)", "ftBnA", Domain.DISTANCE, "ftBnA",
      "British Foot (Benoit 1895 A)", 0.0, 0.304799733333333),

  BRITISH_FOOT_BENOIT_1895_B("British Foot (Benoit 1895 B)", "ftBnB", Domain.DISTANCE, "ftBnB",
      "British Foot (Benoit 1895 B)", 0.0, 0.304799734763271),

  BRITISH_FOOT_1865("British Foot 1865", "ftBr(65)", Domain.DISTANCE, "ftBr(65)", "British Foot 1865", 0.0,
      0.304800833333333),

  IMPERIAL_FOOT("Imperial Foot", "ftCla", Domain.DISTANCE, "ftCla", "Imperial Foot", 0.0, 0.304797265),

  GOLD_COAST_FOOT("Gold Coast Foot", "ftGC", Domain.DISTANCE, "ftGC", "Gold Coast Foot", 0.0, 0.304799710181509),

  INDIAN_FOOT("Indian Foot", "ftInd", Domain.DISTANCE, "ftInd", "Indian Foot", 0.0, 0.304799510248147),

  INDIAN_FOOT_AT_1937("Indian Foot, 1937", "ftInd(37)", Domain.DISTANCE, "ftInd(37)", "Indian Foot, 1937", 0.0,
      0.30479841),

  INDIAN_FOOT_AT_1962("Indian Foot, 1962", "ftInd(62)", Domain.DISTANCE, "ftInd(62)", "Indian Foot, 1962", 0.0,
      0.3047996),

  INDIAN_FOOT_AT_1975("Indian Foot, 1975", "ftInd(75)", Domain.DISTANCE, "ftInd(75)", "Indian Foot, 1975", 0.0,
      0.3047995),

  IRISH_FOOT("Irish foot", "ftIre", Domain.DISTANCE, "ftIre", "Irish Foot", 0.0, 0.3048007491),

  MODIFIED_AMERICAN_FOOT("Modified American Foot", "ftMA", Domain.DISTANCE, "ftMA", "Modified American Foot", 0.0,
      0.304812253),

  SEARS_FOOT("Sears Foot", "ftSe", Domain.DISTANCE, "ftSe", "Sears Foot", 0.0, 0.304799471538676),

  US_SURVEY_FOOT("US Survey Foot", "ftUS", Domain.DISTANCE, "ftUS", "US Survey Foot", 0.0, 0.304800609601219, true),

  GRAM("gram", "g", Domain.MASS, "g", "gram", 0.0, 0.0010),

  GRAM_METER_PER_CUBIC_CENTIMETER_SECOND("gram meter/cubic centimeter second", "g.m/cm3.s",
      Domain.MASS_PER_AREA_PER_TIME, "g.m/cm3.s", "gram feet/cubic centimeter second", 0.0, 0.001),

  GRAM_FEET_PER_CUBIC_CENTIMETER_SECOND("gram feet/cubic centimeter second", "g.ft/cm3.s",
      Domain.MASS_PER_AREA_PER_TIME, "g.ft/cm3.s", "gram feet/cubic centimeter second", 0.0, 304.8),

  GRAMS_PER_LITRE("grams/litre", "g/L", Domain.MASS_PER_VOLUME, "g/L", "grams/litre", 0.0, 1.0),

  GRAMS_PER_CUBIC_CENTIMETER("grams/cubic centimeter", "g/cm3", Domain.MASS_PER_VOLUME, "g/cm3",
      "grams/cubic centimeter", 0.0, 1000.0),

  GRAMS_PER_CENTIMETER_FOURTH("grams/centimeter fourth", "g/cm4", Domain.MASS_PER_VOLUME_PER_LENGTH, "g/cm4",
      "grams/centimeter fourth", 0.0, 100000.0),

  GRAMS_PER_CUBIC_DECIMETER("grams/cubic decimeter", "g/dm3", Domain.MASS_PER_VOLUME, "g/dm3", "grams/cubic decimeter",
      0.0, 1.0),

  GRAMS_PER_UK_GALLON("grams/UK gallon", "g/galUK", Domain.MASS_PER_VOLUME, "g/galUK", "grams/UK gallon", 0.0,
      0.2199692),

  GRAMS_PER_US_GALLON("grams/US gallon", "g/galUS", Domain.MASS_PER_VOLUME, "g/galUS", "grams/US gallon", 0.0, 0.264172),

  GRAMS_PER_KILOGRAM("grams/kilogram", "g/kg", Domain.DIMENSIONLESS, "g/kg", "grams/kilogram", 0.0, 0.0010),

  GRAMS_PER_CUBIC_METER("grams/cubic meter", "g/m3", Domain.MASS_PER_VOLUME, "g/m3", "grams/cubic meter", 0.0, 0.0010),

  GRAMS_PER_SECOND("grams/second", "g/s", Domain.MASS_PER_TIME, "g/s", "grams/second", 0.0, 0.0010),

  API_GAMMA_RAY_UNITS("API gamma ray units", "gAPI", Domain.GAMMA_RAY_API_UNIT, "gAPI", "API gamma ray units", 0.0, 1.0),

  US_GALLONS_PER_94_LB_SACK("US gallons/94 lb sack", "gal/sack", Domain.VOLUME_PER_MASS, "gal/sack",
      "US gallons/94 lb sack", 0.0, 8.87808884580755E-5),

  UK_GALLON("UK gallon", "galUK", Domain.VOLUME, "galUK", "UK gallon", 0.0, 0.004546092),

  UK_GALLONS_PER_1000_BARRELS("UK gallons/1000 barrels", "galUK/Mbbl", Domain.DIMENSIONLESS, "galUK/Mbbl",
      "UK gallons/1000 barrels", 0.0, 2.859406E-5),

  UK_GALLONS_PER_DAY("UK gallons per day", "galUK/d", Domain.FLOWRATE, "galUK/d", "UK gallons per day", 0.0,
      5.265152777777778E-8),

  UK_GALLONS_PER_CUBIC_FOOT("UK gallons/cubic foot", "galUK/ft3", Domain.DIMENSIONLESS, "galUK/ft3",
      "UK gallons/cubic foot", 0.0, 0.1605437),

  UK_GALLONS_PER_HOUR("UK gallons/hour", "galUK/hr", Domain.FLOWRATE, "galUK/hr", "UK gallons/hour", 0.0,
      1.262803333333333E-6),

  UK_GALLONS_PER_HOUR_FOOT("UK gallons/hour foot", "galUK/hr.ft", Domain.AREA_PER_TIME, "galUK/hr.ft",
      "UK gallons/hour foot", 0.0, 4.143055E-6),

  UK_GALLONS_PER_HOUR_SQUARE_FOOT("UK gallons/hour square foot", "galUK/hr.ft2", Domain.VELOCITY, "galUK/hr.ft2",
      "UK gallons/hour square foot", 0.0, 1.35927E-5),

  UK_GALLONS_PER_HOUR_INCH("UK gallons/hour inch", "galUK/hr.in", Domain.AREA_PER_TIME, "galUK/hr.in",
      "UK gallons/hour inch", 0.0, 4.971667E-5),

  UK_GALLONS_PER_HOUR_SQUARE_INCH("UK gallons/hour square inch", "galUK/hr.in2", Domain.VELOCITY, "galUK/hr.in2",
      "UK gallons/hour square inch", 0.0, 0.001957349),

  UK_GALLONS_PER_HOUR_PER_HOUR("UK gallons/hour/hour", "galUK/hr2", Domain.VOLUME_PER_TIME_PER_TIME, "galUK/hr2",
      "UK gallons/hour/hour", 0.0, 3.50778703703704E-10),

  UK_GALLONS_PER_THOUSAND_UK_GALLONS("UK gallons per thousand UK gallons", "galUK/kgalUK", Domain.DIMENSIONLESS,
      "galUK/kgalUK", "UK gallons per thousand UK gallons", 0.0, 0.0010),

  UK_GALLONS_PER_POUND_MASS("UK gallons/pound mass", "galUK/lbm", Domain.VOLUME_PER_MASS, "galUK/lbm",
      "UK gallons/pound mass", 0.0, 0.01002242),

  UK_GALLONS_PER_MILE("UK gallons/mile", "galUK/mi", Domain.AREA, "galUK/mi", "UK gallons/mile", 0.0,
      2.82481060606061E-6),

  UK_GALLONS_PER_MINUTE("UK gallons/minute", "galUK/min", Domain.FLOWRATE, "galUK/min", "UK gallons/minute", 0.0,
      7.57682E-5),

  UK_GALLONS_PER_MINUTE_FOOT("UK gallons/minute foot", "galUK/min.ft", Domain.AREA_PER_TIME, "galUK/min.ft",
      "UK gallons/minute foot", 0.0, 2.485333E-4),

  UK_GALLONS_PER_MINUTE_SQUARE_FOOT("UK gallons/minute square foot", "galUK/min.ft2", Domain.VELOCITY, "galUK/min.ft2",
      "UK gallons/minute square foot", 0.0, 8.155621E-4),

  UK_GALLONS_PER_MINUTE_PER_MINUTE("UK gallons/minute/minute", "galUK/min2", Domain.VOLUME_PER_TIME_PER_TIME,
      "galUK/min2", "UK gallons/minute/minute", 0.0, 1.262803333333333E-6),

  US_GALLONS("US gallons", "galUS", Domain.VOLUME, "galUS", "US gallons", 0.0, 0.003785412),

  US_GALLONS_PER_TEN_BARRELS("US gallons per ten barrels", "galUS/10bbl", Domain.DIMENSIONLESS, "galUS/10bbl",
      "US gallons per ten barrels", 0.0, 0.002380952381),

  US_GALLONS_PER_1000_BARRELS("US gallons/1000 barrels", "galUS/Mbbl", Domain.DIMENSIONLESS, "galUS/Mbbl",
      "US gallons/1000 barrels", 0.0, 2.380952E-5),

  US_GALS_PER_1000_STD_CUBIC_FEET_AT_60_DEG_F("US gals/1000 std cubic feet, 60 deg F", "galUS/Mscf(60F)",
      Domain.VOLUME_PER_STANDARD_VOLUME, "galUS/Mscf(60F)", "US gals/1000 std cubic feet, 60 deg F", 0.0,
      1.339382982105845E-4),

  US_GALLONS_PER_BARRELS("US gallons/barrels", "galUS/bbl", Domain.DIMENSIONLESS, "galUS/bbl", "US gallons/barrels",
      0.0, 0.02380952381),

  US_GALLONS_PER_DAY("US gallons per day", "galUS/d", Domain.FLOWRATE, "galUS/d", "US gallons per day", 0.0,
      4.381263888888889E-8),

  US_GALLONS_PER_FOOT("US gallons/foot", "galUS/ft", Domain.AREA, "galUS/ft", "US gallons/foot", 0.0,
      0.01241933070866142),

  US_GALLONS_PER_CUBIC_FOOT("US gallons/cubic foot", "galUS/ft3", Domain.DIMENSIONLESS, "galUS/ft3",
      "US gallons/cubic foot", 0.0, 0.1336806),

  US_GALLONS_PER_HOUR("US gallons/hour", "galUS/hr", Domain.FLOWRATE, "galUS/hr", "US gallons/hour", 0.0,
      1.051503333333333E-6),

  US_GALLONS_PER_FOOT_HOUR("US gallons/foot hour", "galUS/hr.ft", Domain.AREA_PER_TIME, "galUS/hr.ft",
      "US gallons/foot hour", 0.0, 3.449814E-6),

  US_GALLONS_PER_HOUR_SQUARE_FOOT("US gallons/hour square foot", "galUS/hr.ft2", Domain.VELOCITY, "galUS/hr.ft2",
      "US gallons/hour square foot", 0.0, 1.131829E-5),

  US_GALLONS_PER_HOUR_INCH("US gallons/hour inch", "galUS/hr.in", Domain.AREA_PER_TIME, "galUS/hr.in",
      "US gallons/hour inch", 0.0, 4.139776E-5),

  US_GALLONS_PER_HOUR_SQUARE_INCH("US gallons/hour square inch", "galUS/hr.in2", Domain.VELOCITY, "galUS/hr.in2",
      "US gallons/hour square inch", 0.0, 0.001629833),

  US_GALLONS_PER_HOUR_PER_HOUR("US gallons/hour/hour", "galUS/hr2", Domain.VOLUME_PER_TIME_PER_TIME, "galUS/hr2",
      "US gallons/hour/hour", 0.0, 2.92084259259259E-10),

  US_GALLONS_PER_THOUSAND_US_GALLONS("US gallons per thousand US gallons", "galUS/kgalUS", Domain.DIMENSIONLESS,
      "galUS/kgalUS", "US gallons per thousand US gallons", 0.0, 0.0010),

  US_GALLONS_PER_POUND_MASS("US gallons/pound mass", "galUS/lbm", Domain.VOLUME_PER_MASS, "galUS/lbm",
      "US gallons/pound mass", 0.0, 0.008345404),

  US_GALLONS_PER_MILE("US gallons/mile", "galUS/mi", Domain.AREA, "galUS/mi", "US gallons/mile", 0.0,
      2.35214596754951E-6),

  US_GALLONS_PER_MINUTE("US gallons/minute", "galUS/min", Domain.FLOWRATE, "galUS/min", "US gallons/minute", 0.0,
      6.30902E-5),

  US_GALLONS_PER_MINUTE_FOOT("US gallons/minute foot", "galUS/min.ft", Domain.AREA_PER_TIME, "galUS/min.ft",
      "US gallons/minute foot", 0.0, 2.069888E-4),

  US_GALLONS_PER_MINUTE_SQUARE_FOOT("US gallons/minute square foot", "galUS/min.ft2", Domain.VELOCITY, "galUS/min.ft2",
      "US gallons/minute square foot", 0.0, 6.790972E-4),

  US_GALLONS_PER_MINUTE_PER_MINUTE("US gallons/minute/minute", "galUS/min2", Domain.VOLUME_PER_TIME_PER_TIME,
      "galUS/min2", "US gallons/minute/minute", 0.0, 1.051503333333333E-6),

  US_GALLONS_PER_UK_TON("US gallons/UK ton", "galUS/tonUK", Domain.VOLUME_PER_MASS, "galUS/tonUK", "US gallons/UK ton",
      0.0, 3.725627E-6),

  US_GALLONS_PER_US_TON("US gallons/US ton", "galUS/tonUS", Domain.VOLUME_PER_MASS, "galUS/tonUS", "US gallons/US ton",
      0.0, 4.172702E-6),

  GAMMA("gamma", "gamma", Domain.MAGNETIC_FIELD_STRENGTH, "gamma", "gamma", 0.0, 7.957747E-4),

  GAUSS("gauss", "gauss", Domain.MAGNETIC_FLUX_DENSITY, "gauss", "gauss", 0.0, 1.0E-4),

  GRAM_FORCE("gram force", "gf", Domain.FORCE, "gf", "gram force", 0.0, 0.00980665),

  EARTH_GRAVITY_MULTIPLE("earth gravity multiple", "gn", Domain.ACCELERATION, "gn", "earth gravity multiple", 0.0,
      9.80665),

  GONS("gons", "gon", Domain.PLANE_ANGLE, "gon", "gons", 0.0, 0.015707963267949),

  GRAD("grad", "gr", Domain.PLANE_ANGLE, "gr", "grad", 0.0, 0.015707963267949),

  GRAIN("grain", "grain", Domain.MASS, "grain", "grain", 0.0, 6.479891E-5),

  GRAINS_PER_100_CUBIC_FEET("grains/100 cubic feet", "grain/100ft3", Domain.MASS_PER_VOLUME, "grain/100ft3",
      "grains/100 cubic feet", 0.0, 2.288352E-5),

  GRAINS_PER_CUBIC_FOOT("grains/cubic foot", "grain/ft3", Domain.MASS_PER_VOLUME, "grain/ft3", "grains/cubic foot",
      0.0, 0.002288352),

  GRAINS_PER_US_GALLON("grains/US gallon", "grain/galUS", Domain.MASS_PER_VOLUME, "grain/galUS", "grains/US gallon",
      0.0, 0.01711806),

  GAS_UNIT("gas unit", "gu", Domain.GAS_UNIT, "gu", "gas unit", 0.0, 1.0),

  HOUR("hour", "h", Domain.TIME, "h", "hour", 0.0, 3600.0),

  HOURS_PER_CUBIC_FOOT("hours/cubic foot", "h/ft3", Domain.TIME_PER_VOLUME, "h/ft3", "hours/cubic foot", 0.0,
      127132.7848966252),

  HOUR_PER_THOUSAND_FOOT("hour per thousand foot", "h/kft", Domain.TIME_PER_LENGTH, "h/kft", "hour per thousand foot",
      0.0, 11.811023622047244),

  HOUR_PER_KILOMETER("hour per kilometer", "h/km", Domain.TIME_PER_LENGTH, "h/km", "hour per kilometer", 0.0, 3.6),

  HOUR_PER_CUBIC_METER("hour per cubic meter", "h/m3", Domain.TIME_PER_VOLUME, "h/m3", "hour per cubic meter", 0.0,
      3600.0),

  HECTOLITER("hectoliter", "hL", Domain.VOLUME, "hL", "hectoliter", 0.0, 0.1),

  HECTONEWTON("hectonewton", "hN", Domain.FORCE, "hN", "hectonewton", 0.0, 100.0),

  HECTARE("hectare", "ha", Domain.AREA, "ha", "hectare", 0.0, 10000.0),

  HECTARE_METERS("hectare meters", "ha.m", Domain.VOLUME, "ha.m", "hectare meters", 0.0, 10000.0),

  HECTOBAR("hectobar", "hbar", Domain.PRESSURE, "hbar", "hectobar", 0.0, 1.0E7),

  HECTOGRAM("hectogram", "hg", Domain.MASS, "hg", "hectogram", 0.0, 10.0),

  HYDRAULIC_HORSEPOWER("hydraulic horsepower", "hhp", Domain.POWER, "hhp", "hydraulic horsepower", 0.0, 746.043),

  HYDRAULIC_HORSEPOWER_PER_SQUARE_INCH("(hydraulic) horsepower per square inch", "hhp/in2", Domain.POWER_PER_AREA,
      "hhp/in2", "(hydraulic) horsepower per square inch", 0.0, 1156368.9627379256),

  HECTOMETER("hectometer", "hm", Domain.DISTANCE, "hm", "hectometer", 0.0, 100.0),

  HORSEPOWER("horsepower", "hp", Domain.POWER, "hp", "horsepower", 0.0, 745.6999),

  HORSEPOWER_HOUR("horsepower hour", "hp.hr", Domain.ENERGY, "hp.hr", "horsepower hour", 0.0, 2684520.0),

  HORSEPOWER_HOURS_PER_BARREL("horsepower hours/barrel", "hp.hr/bbl", Domain.MODULUS_OF_COMPRESSION, "hp.hr/bbl",
      "horsepower hours/barrel", 0.0, 1.688512227077257E7),

  HORSEPOWER_HOURS_PER_POUND_MASS("horsepower hours/pound mass", "hp.hr/lbm", Domain.SPECIFIC_ENERGY, "hp.hr/lbm",
      "horsepower hours/pound mass", 0.0, 5918353.12937342),

  HORSEPOWER_PER_CUBIC_FOOT("horsepower/cubic foot", "hp/ft3", Domain.POWER_PER_VOLUME, "hp/ft3",
      "horsepower/cubic foot", 0.0, 26334.14),

  HORSEPOWER_PER_SQUARE_INCH("horsepower per square inch", "hp/in2", Domain.POWER_PER_AREA, "hp/in2",
      "horsepower per square inch", 0.0, 1155837.1566743134),

  HUNDRED_SECONDS("hundred seconds", "hs", Domain.TIME, "hs", "hundred seconds", 0.0, 100.0),

  INCH("inch", "in", Domain.DISTANCE, "in", "inch", 0.0, 0.0254),

  TENTH_OF_AN_INCH("tenth of an inch", "in(1/10)", Domain.DISTANCE, "in/10", "tenth of an inch", 0.0, 0.00254),

  INCHES_PER_YEAR("inches/year", "in/a", Domain.VELOCITY, "in/a", "inches/year", 0.0, 8.0486340295613E-10),

  INCHES_PER_INCH_DEGREE_FAHRENHEIT("inches/inch degree Fahrenheit", "in/in.degF", Domain.INVERSE_TEMPERATURE,
      "in/in.degF", "inches/inch degree Fahrenheit", 0.0, 1.8),

  INCHES_PER_MINUTE("inches/minute", "in/min", Domain.VELOCITY, "in/min", "inches/minute", 0.0, 4.23333333333333E-4),

  INCHES_PER_SECOND("inches/second", "in/s", Domain.VELOCITY, "in/s", "inches/second", 0.0, 0.0254),

  INCHES_PER_FOOT("inches/foot", "in/ft", Domain.DISTANCE, "in/ft", "inches/foot", 0.0, 12.0),

  SQUARE_INCHES("square inches", "in2", Domain.AREA, "in2", "square inches", 0.0, 6.4516E-4),

  SQUARE_INCHES_PER_SQUARE_FOOT("square inches/square foot", "in2/ft2", Domain.DIMENSIONLESS, "in2/ft2",
      "square inches/square foot", 0.0, 0.00694444444444444),

  SQUARE_INCHES_PER_SQUARE_INCH("square inches/square inch", "in2/in2", Domain.DIMENSIONLESS, "in2/in2",
      "square inches/square inch", 0.0, 1.0),

  SQUARE_INCHES_PER_SECOND("square inches/second", "in2/s", Domain.AREA_PER_TIME, "in2/s", "square inches/second", 0.0,
      6.4516E-4),

  CUBIC_INCHES("cubic inches", "in3", Domain.VOLUME, "in3", "cubic inches", 0.0, 1.638706E-5),

  CUBIC_INCHES_PER_FOOT("cubic inches/foot", "in3/ft", Domain.AREA, "in3/ft", "cubic inches/foot", 0.0,
      5.376333333333333E-5),

  INCHES_TO_THE_FOURTH("inches to the fourth", "in4", Domain.MOMENT_OF_SECTION, "in4", "inches to the fourth", 0.0,
      4.162314E-7),

  INCHES_OF_WATER_AT_392_DEG_F("inches of water at 39.2 deg F", "inH2O(39.2F)", Domain.PRESSURE, "inH2O(39.2F)",
      "inches of water at 39.2 deg F", 0.0, 249.082),

  INCHES_OF_WATER_AT_60_DEG_F("inches of water at 60 deg F", "inH2O(60F)", Domain.PRESSURE, "inH2O(60F)",
      "inches of water at 60 deg F", 0.0, 248.84),

  INCHES_OF_MERCURY_AT_32_DEG_F("inches of mercury at 32 deg F", "inHg(32F)", Domain.PRESSURE, "inHg(32F)",
      "inches of mercury at 32 deg F", 0.0, 3386.38),

  INCHES_OF_MERCURY_AT_60_DEG_F("inches of mercury at 60 deg F", "inHg(60F)", Domain.PRESSURE, "inHg(60F)",
      "inches of mercury at 60 deg F", 0.0, 3376.85),

  US_SURVEY_INCH("US Survey inch", "inUS", Domain.DISTANCE, "inUS", "US Survey inch", 0.0, 0.025400050800101603),

  KILOAMPERE("kiloampere", "kA", Domain.ELECTRIC_CURRENT, "kA", "kiloampere", 0.0, 1000.0),

  KILOCOULOMBS("kilocoulombs", "kC", Domain.ELECTRIC_CHARGE, "kC", "kilocoulombs", 0.0, 1000.0),

  KILOEUCLID("kiloeuclid", "kEuc", Domain.DIMENSIONLESS, "kEuc", "kilo euclid", 0.0, 1000.0),

  THOUSAND_PER_SECOND("thousand per second", "kEuc/s", Domain.FREQUENCY, "kEuc/s", "thousand per second", 0.0, 1000.0),

  KILOFARAD("kilofarad", "kF", Domain.CAPACITANCE, "kF", "kilofarad", 0.0, 1000.0),

  KILOGRAY("kilogray", "kGy", Domain.ABSORBED_DOSE, "kGy", "kilogray", 0.0, 1000.0),

  KILOHENRY("kilohenry", "kH", Domain.INDUCTANCE, "kH", "kilohenry", 0.0, 1000.0),

  KILOHERTZ("kilohertz", "kHz", Domain.ROTATIONAL_VELOCITY, "kHz", "kilohertz", 0.0, 6283.185307),

  KILOJOULES("kilojoules", "kJ", Domain.ENERGY, "kJ", "kilojoules", 0.0, 1000.0),

  KILOJOULE_METERS_PER_HOUR_SQ_METER_DEG_K("kilojoule meters/hour sq meter deg K", "kJ.m/h.m2.K",
      Domain.THERMAL_CONDUCTIVITY, "kJ.m/h.m2.K", "kilojoule meters/hour sq meter deg K", 0.0, 0.277777777777778),

  KILOJOULES_PER_CUBIC_DECIMETER("kilojoules/cubic decimeter", "kJ/dm3", Domain.MODULUS_OF_COMPRESSION, "kJ/dm3",
      "kilojoules/cubic decimeter", 0.0, 1000000.0),

  KILOJOULES_PER_HOUR_SQUARE_METER_DEG_K("kilojoules/hour square meter deg K", "kJ/h.m2.K",
      Domain.HEAT_TRANSFER_COEFFICIENT, "kJ/h.m2.K", "kilojoules/hour square meter deg K", 0.0, 0.277777777777778),

  KILOJOULE_PER_KILOGRAM("kilojoule/kilogram", "kJ/kg", Domain.SPECIFIC_ENERGY, "kJ/kg", "kilojoule/kilogram", 0.0,
      1000.0),

  KILOJOULES_PER_KILOGRAM_DEGREE_KELVIN("kilojoules/kilogram degree Kelvin", "kJ/kg.K", Domain.SPECIFIC_HEAT_CAPACITY,
      "kJ/kg.K", "kilojoules/kilogram degree Kelvin", 0.0, 1000.0),

  KILOJOULE_PER_CUBIC_METER("kilojoule/cubic meter", "kJ/m3", Domain.MODULUS_OF_COMPRESSION, "kJ/m3",
      "kilojoule/cubic meter", 0.0, 1000.0),

  KILOJOULE_PER_MOLE_KILOGRAM("kilojoule/mole (kilogram)", "kJ/mol(kg)", Domain.CHEMICAL_POTENTIAL, "kJ/mol(kg)",
      "kilojoule/mole (kilogram)", 0.0, 1000.0),

  KILOJOULES_PER_MOLE_KILOGRAM_DEG_K("kilojoules/mole (kilogram) deg K", "kJ/mol(kg).K", Domain.MOLAR_HEAT_CAPACITY,
      "kJ/mol(kg).K", "kilojoules/mole (kilogram) deg K", 0.0, 1000.0),

  KILONEWTONS("kilonewtons", "kN", Domain.FORCE, "kN", "kilonewtons", 0.0, 1000.0),

  KILONEWTON_METERS("kilonewton meters", "kN.m", Domain.ENERGY, "kN.m", "kilonewton meters", 0.0, 1000.0),

  KILONEWTON_METERS_SQUARED("kilonewton meters squared", "kN.m2", Domain.FORCE_AREA, "kN.m2",
      "kilonewton meters squared", 0.0, 1000.0),

  KILONEWTONS_PER_METER("kilonewtons/meter", "kN/m", Domain.FORCE_PER_LENGTH, "kN/m", "kilonewtons/meter", 0.0, 1000.0),

  KILONEWTONS_PER_SQUARE_METER("kilonewtons/square meter", "kN/m2", Domain.PRESSURE, "kN/m2",
      "kilonewtons/square meter", 0.0, 1000.0),

  KILOPOISE("kilopoise", "kP", Domain.DYNAMIC_VISCOSITY, "kP", "kilopoise", 0.0, 100.0),

  KILOPASCALS("kilopascals", "kPa", Domain.PRESSURE, "kPa", "kilopascals", 0.0, 1000.0),

  KILOPASCAL_SECONDS_PER_METER("kilopascal seconds/meter", "kPa.s/m", Domain.MASS_PER_AREA_PER_TIME, "kPa.s/m",
      "kilopascal seconds/meter", 0.0, 1000.0),

  KILOPASCAL_PER_HUNDRED_METER("kilopascal per hundred meter", "kPa/100m", Domain.FORCE_PER_VOLUME, "kPa/100m",
      "kilopascal per hundred meter", 0.0, 10.0),

  KILOPASCAL_PER_HOUR("kilopascal per hour", "kPa/h", Domain.PRESSURE_PER_TIME, "kPa/h", "kilopascal per hour", 0.0,
      0.2777777777777778),

  KILOPASCALS_PER_METER("kilopascals/meter", "kPa/m", Domain.PRESSURE_PER_LENGTH, "kPa/m", "kilopascals/meter", 0.0,
      1000.0),

  KILOPASCAL_PER_MIN("kilopascal per min", "kPa/min", Domain.PRESSURE_PER_TIME, "kPa/min", "kilopascal per min", 0.0,
      16.666666666666668),

  KILOPASCAL_SQUARED("kilopascal squared", "kPa2", Domain.PRESSURE_SQUARED, "kPa2", "kilopascal squared", 0.0,
      1000000.0),

  KILOPASCAL_SQUARED_PER_CENTIPOISE("kilopascal squared per centipoise", "kPa2/cP", Domain.PRESSURE_PER_TIME,
      "kPa2/cP", "kilopascal squared per centipoise", 0.0, 1.0E9),

  KILOPASCAL_SQUARED_PER_THOUSAND_CENTIPOISE("kilopascal squared per thousand centipoise", "kPa2/kcP",
      Domain.PRESSURE_PER_TIME, "kPa2/kcP", "kilopascal squared per thousand centipoise", 0.0, 1000000.0),

  KILOSIEMENS("kilosiemens", "kS", Domain.ELECTRIC_CONDUCTANCE, "kS", "kilosiemens", 0.0, 1000.0),

  KILOTESLA("kilotesla", "kT", Domain.MAGNETIC_FLUX_DENSITY, "kT", "kilotesla", 0.0, 1000.0),

  KILOVOLT("kilovolt", "kV", Domain.ELECTRIC_POTENTIAL, "kV", "kilovolt", 0.0, 1000.0),

  KILOWATTS("kilowatts", "kW", Domain.POWER, "kW", "kilowatts", 0.0, 1000.0),

  KILOWATT_HOURS("kilowatt hours", "kW.h", Domain.ENERGY, "kW.h", "kilowatt hours", 0.0, 3600000.0),

  KILOWATT_HOURS_PER_DECIMETER("kilowatt hours/decimeter", "kW.h/dm3", Domain.MODULUS_OF_COMPRESSION, "kW.h/dm3",
      "kilowatt hours/decimeter", 0.0, 3.6E9),

  KILOWATT_HOURS_PER_KILOGRAM("kilowatt hours/kilogram", "kW.h/kg", Domain.SPECIFIC_ENERGY, "kW.h/kg",
      "kilowatt hours/kilogram", 0.0, 3600000.0),

  KILOWATT_HOURS_PER_KILOGRAM_DEGREE_C("kilowatt hours/kilogram degree C", "kW.h/kg.degC",
      Domain.SPECIFIC_HEAT_CAPACITY, "kW.h/kg.degC", "kilowatt hours/kilogram degree C", 0.0, 3600000.0),

  KILOWATT_HOURS_PER_CUBIC_METERS("kilowatt hours/cubic meters", "kW.h/m3", Domain.MODULUS_OF_COMPRESSION, "kW.h/m3",
      "kilowatt hours/cubic meters", 0.0, 3600000.0),

  KILOWATTS_PER_SQUARE_CENTIMETER("kilowatts/square centimeter", "kW/cm2", Domain.POWER_PER_AREA, "kW/cm2",
      "kilowatts/square centimeter", 0.0, 1.0E7),

  KILOWATTS_PER_SQUARE_METER("kilowatts/square meter", "kW/m2", Domain.POWER_PER_AREA, "kW/m2",
      "kilowatts/square meter", 0.0, 1000.0),

  KILOWATTS_PER_SQUARE_METER_DEGREE_KELVIN("kilowatts/square meter degree Kelvin", "kW/m2.K",
      Domain.HEAT_TRANSFER_COEFFICIENT, "kW/m2.K", "kilowatts/square meter degree Kelvin", 0.0, 1000.0),

  KILOWATTS_PER_CUBIC_METER("kilowatts/cubic meter", "kW/m3", Domain.POWER_PER_VOLUME, "kW/m3",
      "kilowatts/cubic meter", 0.0, 1000.0),

  KILLOWATTS_PER_CUBIC_METER_DEGREE_KELVIN("killowatts/cubic meter degree Kelvin", "kW/m3.K",
      Domain.WATTS_PER_CUBIC_METER_KELVIN, "kW/m3.K", "killowatts/cubic meter degree Kelvin", 0.0, 1000.0),

  KILOWEBER("kiloweber", "kWb", Domain.MAGNETIC_FLUX, "kWb", "kiloweber", 0.0, 1000.0),

  KILOYEAR("kiloyear", "ka", Domain.TIME, "ka", "kiloyear", 0.0, 3.155815E10),

  KILOBYTE("kilobyte", "kbyte", Domain.DIGITAL_STORAGE, "kbyte", "kilobyte", 0.0, 1024.0),

  KILOCALORIES("kilocalories", "kcal", Domain.ENERGY, "kcal", "kilocalories", 0.0, 4184.0),

  KILOCALORIE_METERS_PER_SQUARE_CENTIMETER("kilocalorie meters/square centimeter", "kcal.m/cm2", Domain.FORCE,
      "kcal.m/cm2", "kilocalorie meters/square centimeter", 0.0, 4.184E7),

  KILOCALORIES_PER_CUBIC_CENTIMETER("kilocalories/cubic centimeter", "kcal/cm3", Domain.MODULUS_OF_COMPRESSION,
      "kcal/cm3", "kilocalories/cubic centimeter", 0.0, 4.184E9),

  KILOCALORIES_PER_GRAM("kilocalories/gram", "kcal/g", Domain.SPECIFIC_ENERGY, "kcal/g", "kilocalories/gram", 0.0,
      4184000.0),

  KILOCALORIES_PER_HOUR("kilocalories/hour", "kcal/h", Domain.POWER, "kcal/h", "kilocalories/hour", 0.0, 1.162222),

  KILOCALORIES_PER_HOUR_METER_DEGREE_CELSIUS("kilocalories/hour meter degree Celsius", "kcal/h.m.degC",
      Domain.THERMAL_CONDUCTIVITY, "kcal/h.m.degC", "kilocalories/hour meter degree Celsius", 0.0, 1.162222),

  KILOCALORIE_PER_HOUR_SQUARE_METER_DEG_C("kilocalorie/hour square meter deg C", "kcal/h.m2.degC",
      Domain.HEAT_TRANSFER_COEFFICIENT, "kcal/h.m2.degC", "kilocalorie/hour square meter deg C", 0.0, 1.162222),

  KILOCALORIES_PER_KILOGRAM("kilocalories/kilogram", "kcal/kg", Domain.SPECIFIC_ENERGY, "kcal/kg",
      "kilocalories/kilogram", 0.0, 4184.0),

  KILOCALORIES_PER_KILOGRAM_DEGREE_CELSIUS("kilocalories/kilogram degree Celsius", "kcal/kg.degC",
      Domain.SPECIFIC_HEAT_CAPACITY, "kcal/kg.degC", "kilocalories/kilogram degree Celsius", 0.0, 4184.0),

  KILOCALROIES_PER_CUBIC_METER("kilocalroies/cubic meter", "kcal/m3", Domain.MODULUS_OF_COMPRESSION, "kcal/m3",
      "kilocalroies/cubic meter", 0.0, 4184.0),

  KILOCALORIES_PER_MOLE_GRAM("kilocalories/mole (gram)", "kcal/mol(g)", Domain.CHEMICAL_POTENTIAL, "kcal/mol(g)",
      "kilocalories/mole (gram)", 0.0, 4184000.0),

  KILOCANDELA("kilocandela", "kcd", Domain.LUMINOUS_INTENSITY, "kcd", "kilocandela", 0.0, 1000.0),

  KILKODYNES("kilkodynes", "kdyne", Domain.FORCE, "kdyne", "kilkodynes", 0.0, 0.01),

  KILOELECTRON_VOLTS("kiloelectron volts", "keV", Domain.ENERGY, "keV", "kiloelectron volts", 0.0, 1.60219E-16),

  THOUSAND_FOOT_POUNDS_FORCE("thousand foot pounds force", "kft.lbf", Domain.ENERGY, "kft.lbf",
      "thousand foot pounds force", 0.0, 1355818.0),

  THOUSAND_FEET_PER_HOUR("thousand feet per hour", "kft/h", Domain.VELOCITY, "kft/h", "thousand feet per hour", 0.0,
      0.08466666666666667),

  THOUSAND_FEET_PER_SECOND("thousand feet per second", "kft/s", Domain.VELOCITY, "kft/s", "thousand feet per second",
      0.0, 304.8),

  KILOFEET("kilofeet", "kft", Domain.DISTANCE, "kft", "kilofeet", 0.0, 304.8),

  KILOFEET_PER_FOOT("kilofeet/foot", "kft/ft", Domain.DIMENSIONLESS, "kft/ft", "kilofeet/foot", 0.0, 1000.0),

  KILOGRAM("kilogram", "kg", Domain.MASS, "kg", "kilogram", 0.0, 1.0),

  METER_KILOGRAM("meter-kilogram", "kg.m", Domain.MASS_LENGTH, "kg.m", "meter-kilogram", 0.0, 1.0),

  KILOGRAM_METERS_PER_SQUARE_CENTIMETER("kilogram meters/square centimeter", "kg.m/cm2", Domain.MASS_PER_LENGTH,
      "kg.m/cm2", "kilogram meters/square centimeter", 0.0, 10000.0),

  KILOGRAM_METERS_PER_SECOND("kilogram meters/second", "kg.m/s", Domain.MOMENTUM, "kg.m/s", "kilogram meters/second",
      0.0, 1.0),

  KILOGRAM_METERS_SQUARED("kilogram meters squared", "kg.m2", Domain.MOMENT_OF_INERTIA, "kg.m2",
      "kilogram meters squared", 0.0, 1.0),

  KILOGRAM_PER_METER_SECOND("kilogram per meter second", "kg/(m.s)", Domain.MASS_PER_LENGTH_PER_TIME, "kg/(m.s)",
      "kilogram per meter second", 0.0, 1.0),

  KILOGRAMS_PER_JOULE("kilograms/joule", "kg/J", Domain.MASS_PER_ENERGY, "kg/J", "kilograms/joule", 0.0, 1.0),

  KILOGRAM_PER_LITRE("kilogram per litre", "kg/L", Domain.MASS_PER_VOLUME, "kg/L", "kilogram per litre", 0.0, 1000.0),

  KILOGRAMS_PER_MEGAJOULE("kilograms/megajoule", "kg/MJ", Domain.MASS_PER_ENERGY, "kg/MJ", "kilograms/megajoule", 0.0,
      1.0E-6),

  KILOGRAM_PER_DAY("kilogram per day", "kg/d", Domain.MASS_PER_TIME, "kg/d", "kilogram per day", 0.0,
      1.1574074074074073E-5),

  KILOGRAMS_PER_CUBIC_DECIMETER("kilograms/cubic decimeter", "kg/dm3", Domain.MASS_PER_VOLUME, "kg/dm3",
      "kilograms/cubic decimeter", 0.0, 1000.0),

  KILOGRAMS_PER_DECIMETER_FOURTH("kilograms/decimeter fourth", "kg/dm4", Domain.MASS_PER_VOLUME_PER_LENGTH, "kg/dm4",
      "kilograms/decimeter fourth", 0.0, 10000.0),

  KILOGRAMS_PER_HOUR("kilograms/hour", "kg/h", Domain.MASS_PER_TIME, "kg/h", "kilograms/hour", 0.0, 2.77777777777778E-4),

  KILOGRAMS_PER_KILOWATT_HOUR("kilograms/kilowatt hour", "kg/kW.h", Domain.MASS_PER_ENERGY, "kg/kW.h",
      "kilograms/kilowatt hour", 0.0, 2.77777777777778E-7),

  KILOGRAMS_PER_KILOGRAM("kilograms/kilogram", "kg/kg", Domain.DIMENSIONLESS, "kg/kg", "kilograms/kilogram", 0.0, 1.0),

  KILOGRAMS_PER_METER("kilograms/meter", "kg/m", Domain.MASS_PER_LENGTH, "kg/m", "kilograms/meter", 0.0, 1.0),

  KILOGRAMS_PER_METER_SECOND("kilograms/meter second", "kg/m.s", Domain.KILOGRAMS_PER_METER_SECOND, "kg/m.s",
      "kilograms/meter second", 0.0, 1.0),

  KILOGRAMS_PER_SQUARE_METER("kilograms/square meter", "kg/m2", Domain.MASS_PER_AREA, "kg/m2",
      "kilograms/square meter", 0.0, 1.0),

  KILOGRAMS_PER_SQUARE_METER_SECONDS("kilograms/square meter seconds", "kg/m2.s", Domain.MASS_PER_AREA_PER_TIME,
      "kg/m2.s", "kilograms/square meter seconds", 0.0, 1.0),

  KILOGRAMS_PER_CUBIC_METER("kilograms/cubic meter", "kg/m3", Domain.MASS_PER_VOLUME, "kg/m3", "kilograms/cubic meter",
      0.0, 1.0),

  KILOGRAM_PER_METER_FOURTH("kilogram/meter fourth", "kg/m4", Domain.MASS_PER_VOLUME_PER_LENGTH, "kg/m4",
      "kilogram/meter fourth", 0.0, 1.0),

  KILOGRAM_PER_MIN("kilogram per min", "kg/min", Domain.MASS_PER_TIME, "kg/min", "kilogram per min", 0.0,
      0.016666666666666666),

  KILOGRAMS_PER_SECOND("kilograms/second", "kg/s", Domain.MASS_PER_TIME, "kg/s", "kilograms/second", 0.0, 1.0),

  KILOGRAM_PER_94_POUND_SACK("kilogram per 94 pound sack", "kg/sack94", Domain.DIMENSIONLESS, "kg/sack94",
      "kilogram per 94 pound sack", 0.0, 0.023453428175869755),

  KILOGRAMS_PER_TONNE("kilograms per tonne", "kg/t", Domain.DIMENSIONLESS, "t", "kilograms per tonne", 0.0, 0.0010),

  KILOGAMMA("kilogamma", "kgamma", Domain.MAGNETIC_FIELD_STRENGTH, "kgamma", "kilogamma", 0.0, 0.7957747),

  KILOGAUSS("kilogauss", "kgauss", Domain.MAGNETIC_FLUX_DENSITY, "kgauss", "kilogauss", 0.0, 0.1),

  KILOGRAM_FORCE("kilogram force", "kgf", Domain.FORCE, "kgf", "kilogram force", 0.0, 9.80665),

  KILOGRAM_FORCE_METERS("kilogram force meters", "kgf.m", Domain.ENERGY, "kgf.m", "kilogram force meters", 0.0, 9.80665),

  KILOGRAM_FORCE_METERS_PER_SQUARE_CENTIMETER("kilogram force meters/square centimeter", "kgf.m/cm2",
      Domain.FORCE_PER_LENGTH, "kgf.m/cm2", "kilogram force meters/square centimeter", 0.0, 98066.5),

  KILOGRAMS_FORCE_METERS_PER_METER("kilograms force meters/meter", "kgf.m/m", Domain.FORCE, "kgf.m/m",
      "kilograms force meters/meter", 0.0, 9.80665),

  KILOGRAM_FORCE_METERS_SQUARED("kilogram force meters squared", "kgf.m2", Domain.FORCE_AREA, "kgf.m2",
      "kilogram force meters squared", 0.0, 9.80665),

  KILOGRAMS_FORCE_SECONDS_PER_SQUARE_METER("kilograms force seconds/square meter", "kgf.s/m2",
      Domain.DYNAMIC_VISCOSITY, "kgf.s/m2", "kilograms force seconds/square meter", 0.0, 9.80665),

  KILOGRAMS_FORCE_PER_CENTIMETER("kilograms force/centimeter", "kgf/cm", Domain.FORCE_PER_LENGTH, "kgf/cm",
      "kilograms force/centimeter", 0.0, 980.665),

  KILOGRAM_PER_SQUARE_CENTIMETER("kilogram per square centimeter", "kgf/cm2", Domain.PRESSURE, "kgf/cm2",
      "kilogram per square centimeter", 0.0, 98066.49999999999),

  KILOGRAM_FORCE_PER_KILOGRAM_FORCE("kilogram force per kilogram force", "kgf/kgf", Domain.DIMENSIONLESS, "kgf/kgf",
      "kilogram force per kilogram force", 0.0, 1.0),

  KILOGRAM_FORCE_PER_SQUARE_MILLIMETER("kilogram force/square millimeter", "kgf/mm2", Domain.PRESSURE, "kgf/mm2",
      "kilogram force/square millimeter", 0.0, 9806650.0),

  THOUSAND_POUNDS_FORCE("thousand pounds force", "klbf", Domain.FORCE, "klbf", "thousand pounds force", 0.0, 4448.222),

  THOUSAND_POUNDS_MASS("thousand pounds mass", "klbm", Domain.MASS, "klbm", "thousand pounds mass", 0.0, 453.5924),

  THOUSAND_POUNDS_MASS_PER_INCH("thousand pounds mass per inch", "klbm/in", Domain.MASS_PER_LENGTH, "klbm/in",
      "thousand pounds mass per inch", 0.0, 17857.96850393701),

  KILOLUX("kilolux", "klx", Domain.ILLUMINANCE, "klx", "kilolux", 0.0, 1000.0),

  KILOMETER("kilometer", "km", Domain.DISTANCE, "km", "kilometer", 0.0, 1000.0, true),

  KILOMETERS_PER_METER("kilometers/meter", "km/m", Domain.DIMENSIONLESS, "km/m", "kilometers/meter", 0.0, 1000.0),

  KILOMETERS_PER_LITRE("kilometers/litre", "km/L", Domain.INVERSE_AREA, "km/L", "kilometers/litre", 0.0, 1000000.0),

  KILOMETER_PER_CENTIMETER("kilometer/ centimeter", "km/cm", Domain.DIMENSIONLESS, "km/cm", "kilometer/ centimeter",
      0.0, 100000.0),

  KILOMETERS_PER_CUBIC_DECIMETER("kilometers/cubic decimeter", "km/dm3", Domain.INVERSE_AREA, "km/dm3",
      "kilometers/cubic decimeter", 0.0, 1000000.0),

  KILOMETERS_PER_HOUR("kilometers/hour", "km/h", Domain.VELOCITY, "km/h", "kilometers/hour", 0.0, 0.277777777777778),

  KILOMETERS_PER_SECOND("kilometers per second", "km/s", Domain.VELOCITY, "km/s", "kilometers per second", 0.0, 1000.0,
      true),

  SQUARE_KILOMETERS("square kilometers", "km2", Domain.AREA, "km2", "square kilometers", 0.0, 1000000.0),

  CUBIC_KILOMETERS("cubic kilometers", "km3", Domain.VOLUME, "km3", "cubic kilometers", 0.0, 1.0E9),

  KILOMHO("kilomho", "kmho", Domain.ELECTRIC_CONDUCTANCE, "kmho", "kilomho", 0.0, 1000.0),

  KILOMOLE("kilomole", "kmol", Domain.AMOUNT_OF_SUBSTANCE, "kmol", "kilomole", 0.0, 1000.0),

  KNOTS("knots", "knot", Domain.VELOCITY, "knot", "knots", 0.0, 0.514444444444444),

  KILOHM("kilohm", "kohm", Domain.ELECTRIC_RESISTANCE, "kohm", "kilohm", 0.0, 1000.0),

  KILO_OHM_METER("kilo ohm meter", "kohm.m", Domain.RESISTIVITY, "kohm.m", "kilo ohm meter", 0.0, 1000.0),

  THOUSAND_POUNDS_PER_SQUARE_INCH("thousand pounds per square inch", "kpsi", Domain.PRESSURE, "kpsi",
      "thousand pounds per square inch", 0.0, 6894757.0),

  THOUSAND_POUND_PER_SQUARE_INCH_AT_SQUARED("thousand pound per square inch, squared", "kpsi2",
      Domain.PRESSURE_SQUARED, "kpsi2", "thousand pound per square inch, squared", 0.0, 4.7537674E13),

  KILORADIAN("kiloradian", "krad", Domain.PLANE_ANGLE, "krad", "kiloradian", 0.0, 1000.0),

  KILORAD("kilorad", "krd", Domain.ABSORBED_DOSE, "krd", "kilorad", 0.0, 10.0),

  STANDARD_KILOMETER("standard kilometer", "ksm", Domain.DISTANCE, "ksm", "standard kilometer", 0.0, 1000.0),

  KILO_STANDARD_CUBIC_METERS_15C("kilo standard cubic meters 15C", "ksm3", Domain.STANDARD_VOLUME, "ksm3",
      "kilo standard cubic meters 15C", 0.0, 1000.0),

  THOUSAND_STD_CUBIC_METERS_PER_DAY("thousand std cubic meters/ day", "ksm3/d", Domain.STANDARD_VOLUME_PER_TIME,
      "ksm3/d", "thousand std cubic meters/ day", 0.0, 0.01157407407407407),

  POUNDS_FORCE("pounds force", "lbf", Domain.FORCE, "lbf", "pounds force", 0.0, 4.448222),

  POUNDS_FORCE_FEET_PER_INCH("pounds force feet/inch", "lbf.ft/in", Domain.FORCE, "lbf.ft/in",
      "pounds force feet/inch", 0.0, 53.3786614173228),

  FOOT_POUNDS_FORCE_PER_SQUARE_INCH("foot pounds force/square inch", "lbf.ft/in2", Domain.FORCE_PER_LENGTH,
      "lbf.ft/in2", "foot pounds force/square inch", 0.0, 2101.522),

  INCH_POUNDS_FORCE("inch pounds force", "lbf.in", Domain.ENERGY, "lbf.in", "inch pounds force", 0.0, 0.1129848),

  POUNDS_FORCE_INCHES_PER_INCH("pounds force inches/inch", "lbf.in/in", Domain.FORCE, "lbf.in/in",
      "pounds force inches/inch", 0.0, 4.448222),

  POUNDS_FORCE_INCHES_SQUARED("pounds force inches squared", "lbf.in2", Domain.FORCE_AREA, "lbf.in2",
      "pounds force inches squared", 0.0, 0.002869815),

  POUNDS_FORCE_SECONDS_PER_SQUARE_FOOT("pounds force seconds/square foot", "lbf.s/ft2", Domain.DYNAMIC_VISCOSITY,
      "lbf.s/ft2", "pounds force seconds/square foot", 0.0, 47.88026),

  POUNDS_FORCE_SECONDS_PER_SQUARE_INCH("pounds force seconds/square inch", "lbf.s/in2", Domain.DYNAMIC_VISCOSITY,
      "lbf.s/in2", "pounds force seconds/square inch", 0.0, 6894.757),

  POUNDS_FORCE_PER_HUNDRED_FOOT("pounds force per hundred foot", "lbf/100ft", Domain.FORCE_PER_LENGTH, "lbf/100ft",
      "pounds force per hundred foot", 0.0, 0.14593904199475066),

  POUNDS_FORCE_PER_100_SQUARE_FOOT("pounds force/100 square foot", "lbf/100ft2", Domain.PRESSURE, "lbf/100ft2",
      "pounds force/100 square foot", 0.0, 0.4788026),

  POUNDS_FORCE_PER_THIRTY_METERS("pounds force per thirty meters", "lbf/30m", Domain.FORCE_PER_LENGTH, "lbf/30m",
      "pounds force per thirty meters", 0.0, 0.14827406666666668),

  POUNDS_FORCE_PER_FOOT("pounds force per foot", "lbf/ft", Domain.FORCE_PER_LENGTH, "lbf/ft", "pounds force per foot",
      0.0, 14.593904199475066),

  POUNDS_FORCE_PER_SQUARE_FOOT("pounds force/square foot", "lbf/ft2", Domain.PRESSURE, "lbf/ft2",
      "pounds force/square foot", 0.0, 47.88026),

  POUNDS_FORCE_PER_CUBIC_FOOT("pounds force/cubic foot", "lbf/ft3", Domain.FORCE_PER_VOLUME, "lbf/ft3",
      "pounds force/cubic foot", 0.0, 157.0874585273433),

  POUNDS_FORCE_PER_US_GALLON("pounds force/US gallon", "lbf/galUS", Domain.FORCE_PER_VOLUME, "lbf/galUS",
      "pounds force/US gallon", 0.0, 1175.09586803233),

  POUNDS_FORCE_PER_INCH("pounds force/inch", "lbf/in", Domain.FORCE_PER_LENGTH, "lbf/in", "pounds force/inch", 0.0,
      175.1268503937008),

  POUNDS_FORCE_PER_SQUARE_INCH("pounds force/square inch", "lbf/in2", Domain.PRESSURE, "lbf/in2",
      "pounds force/square inch", 0.0, 6894.757),

  POUND_FORCE_PER_POUND_FORCE("pound force per pound force", "lbf/lbf", Domain.DIMENSIONLESS, "lbf/lbf",
      "pound force per pound force", 0.0, 1.0),

  POUNDS_MASS("pounds mass", "lbm", Domain.MASS, "lbm", "pounds mass", 0.0, 0.4535924),

  FOOT_POUNDS_MASS_PER_SECOND("foot pounds mass/second", "lbm.ft/s", Domain.MOMENTUM, "lbm.ft/s",
      "foot pounds mass/second", 0.0, 0.138255),

  POUNDS_MASS_SQUARE_FEET("pounds mass square feet", "lbm.ft2", Domain.MOMENT_OF_INERTIA, "lbm.ft2",
      "pounds mass square feet", 0.0, 0.04214011),

  POUNDS_MASS_SQUARE_FEET_PER_SECOND_SQUARED("pounds mass square feet/second squared", "lbm.ft2/s2", Domain.ENERGY,
      "lbm.ft2/s2", "pounds mass square feet/second squared", 0.0, 0.04214011),

  POUNDS_MASS_PER_1000_UK_GALLONS("pounds mass/1000 UK gallons", "lbm/1000galUK", Domain.MASS_PER_VOLUME,
      "lbm/1000galUK", "pounds mass/1000 UK gallons", 0.0, 0.09977633),

  POUNDS_MASS_PER_1000_US_GALLONS("pounds mass/1000 US gallons", "lbm/1000galUS", Domain.MASS_PER_VOLUME,
      "lbm/1000galUS", "pounds mass/1000 US gallons", 0.0, 0.11982640000000001),

  POUNDS_MASS_PER_HUNDRED_SQUARE_FOOT("pounds mass per hundred square foot", "lbm/100ft2", Domain.MASS_PER_AREA,
      "lbm/100ft2", "pounds mass per hundred square foot", 0.0, 0.04882428),

  POUNDS_MASS_PER_10_BARREL("pounds mass per 10 barrel", "lbm/10bbl", Domain.MASS_PER_VOLUME, "lbm/10bbl",
      "pounds mass per 10 barrel", 0.0, 0.28530102718896416),

  POUNDS_MASS_PER_1000_BARRELS("pounds mass/1000 barrels", "lbm/Mbbl", Domain.MASS_PER_VOLUME, "lbm/Mbbl",
      "pounds mass/1000 barrels", 0.0, 0.00285301),

  POUNDS_MASS_PER_BARREL("pounds mass/barrel", "lbm/bbl", Domain.MASS_PER_VOLUME, "lbm/bbl", "pounds mass/barrel", 0.0,
      2.85301),

  POUND_MASS_PER_DAY("pound mass per day", "lbm/d", Domain.MASS_PER_TIME, "lbm/d", "pound mass per day", 0.0,
      5.249912037037037E-6),

  POUNDS_MASS_PER_FOOT("pounds mass/foot", "lbm/ft", Domain.MASS_PER_LENGTH, "lbm/ft", "pounds mass/foot", 0.0,
      1.488164),

  POUNDS_MASS_PER_FOOT_HOUR("pounds mass/foot hour", "lbm/ft.h", Domain.DYNAMIC_VISCOSITY, "lbm/ft.h",
      "pounds mass/foot hour", 0.0, 4.133789E-4),

  POUNDS_MASS_PER_FOOT_SECOND("pounds mass/foot second", "lbm/ft.s", Domain.DYNAMIC_VISCOSITY, "lbm/ft.s",
      "pounds mass/foot second", 0.0, 1.488164),

  POUNDS_MASS_PER_SQUARE_FOOT("pounds mass/square foot", "lbm/ft2", Domain.MASS_PER_AREA, "lbm/ft2",
      "pounds mass/square foot", 0.0, 4.882428),

  POUNDS_MASS_PER_CUBIC_FOOT("pounds mass/cubic foot", "lbm/ft3", Domain.MASS_PER_VOLUME, "lbm/ft3",
      "pounds mass/cubic foot", 0.0, 16.01846),

  POUNDS_MASS_PER_FOOT_FOURTH("pounds mass/foot fourth", "lbm/ft4", Domain.MASS_PER_VOLUME_PER_LENGTH, "lbm/ft4",
      "pounds mass/foot fourth", 0.0, 52.5540026246719),

  POUNDS_MASS_PER_UK_GALLON("pounds mass/UK gallon", "lbm/galUK", Domain.MASS_PER_VOLUME, "lbm/galUK",
      "pounds mass/UK gallon", 0.0, 99.77633),

  POUNDS_MASS_PER_UK_GALLON_FOOT("pounds mass/UK gallon foot", "lbm/galUK.ft", Domain.MASS_PER_VOLUME_PER_LENGTH,
      "lbm/galUK.ft", "pounds mass/UK gallon foot", 0.0, 327.350164041995),

  POUNDS_MASS_PER_US_GALLON("pounds mass/US gallon", "lbm/galUS", Domain.MASS_PER_VOLUME, "lbm/galUS",
      "pounds mass/US gallon", 0.0, 119.8264),

  POUNDS_MASS_PER_US_GALLON_FOOT("pounds mass/US gallon foot", "lbm/galUS.ft", Domain.MASS_PER_VOLUME_PER_LENGTH,
      "lbm/galUS.ft", "pounds mass/US gallon foot", 0.0, 393.131233595801),

  POUNDS_MASS_PER_HOUR("pounds mass/hour", "lbm/h", Domain.MASS_PER_TIME, "lbm/h", "pounds mass/hour", 0.0,
      1.259978888888889E-4),

  POUNDS_MASS_PER_HOUR_FOOT("pounds mass/hour foot", "lbm/h.ft", Domain.DYNAMIC_VISCOSITY, "lbm/h.ft",
      "pounds mass/hour foot", 0.0, 4.133789E-4),

  POUNDS_MASS_PER_HOUR_SQUARE_FOOT("pounds mass/hour square foot", "lbm/h.ft2", Domain.MASS_PER_AREA_PER_TIME,
      "lbm/h.ft2", "pounds mass/hour square foot", 0.0, 0.00135623),

  POUNDS_MASS_PER_HORSEPOWER_HOUR("pounds mass/horsepower hour", "lbm/hp.h", Domain.MASS_PER_ENERGY, "lbm/hp.h",
      "pounds mass/horsepower hour", 0.0, 1.689659E-7),

  POUNDS_MASS_PER_CUBIC_INCH("pounds mass/cubic inch", "lbm/in3", Domain.MASS_PER_VOLUME, "lbm/in3",
      "pounds mass/cubic inch", 0.0, 27679.906540915446),

  POUNDS_MASS_PER_MINUTE("pounds mass/minute", "lbm/min", Domain.MASS_PER_TIME, "lbm/min", "pounds mass/minute", 0.0,
      0.007559873),

  POUNDS_MASS_PER_SECOND("pounds mass/second", "lbm/s", Domain.MASS_PER_TIME, "lbm/s", "pounds mass/second", 0.0,
      0.4535924),

  POUNDS_MASS_PER_SECOND_FOOT("pounds mass/second foot", "lbm/s.ft", Domain.DYNAMIC_VISCOSITY, "lbm/s.ft",
      "pounds mass/second foot", 0.0, 1.488164),

  POUNDS_MASS_PER_SECOND_SQUARE_FOOT("pounds mass/second square foot", "lbm/s.ft2", Domain.MASS_PER_AREA_PER_TIME,
      "lbm/s.ft2", "pounds mass/second square foot", 0.0, 4.882428),

  BRITISH_LINK_1895_A("British link 1895 A", "lkBnA", Domain.DISTANCE, "lkBnA", "British link 1895 A", 0.0, 0.201167824),

  BRITISH_LINK_1895_B("British link 1895 B", "lkBnB", Domain.DISTANCE, "lkBnB", "British link 1895 B", 0.0,
      0.2011678249437587),

  CLARKE_LINK("Clarke link", "lkCla", Domain.DISTANCE, "lkCla", "Clarke link", 0.0, 0.2011661949759657),

  SEARS_LINK("Sears link", "lkSe", Domain.DISTANCE, "lkSe", "Sears link", 0.0, 0.2011676512155263),

  US_SURVEY_LINK("US Survey link", "lkUS", Domain.DISTANCE, "lkUS", "US Survey link", 0.0, 0.2011684023368047),

  LUMEN("lumen", "lm", Domain.LUMINOUS_FLUX, "lm", "lumen", 0.0, 1.0),

  LUMEN_SECOND("lumen second", "lm.s", Domain.QUANTITY_OF_LIGHT, "lm.s", "lumen second", 0.0, 1.0),

  LUMENS_PER_WATT("lumens/watt", "lm/W", Domain.LUMENS_PER_WATT, "lm/W", "lumens/watt", 0.0, 1.0),

  LUMENS_PER_SQUARE_METER("lumens/square meter", "lm/m2", Domain.ILLUMINANCE, "lm/m2", "lumens/square meter", 0.0, 1.0),

  LUX("lux", "lx", Domain.ILLUMINANCE, "lx", "lux", 0.0, 1.0),

  LUX_SECONDS("lux seconds", "lx.s", Domain.LIGHT_EXPOSURE, "lx.s", "lux seconds", 0.0, 1.0),

  METER("meter", "m", Domain.DISTANCE, "m", "meter", 0.0, 1.0, true),

  METER_NEWTON("meter newton", "m.N", Domain.ENERGY, "m.N", "meter newton", 0.0, 1.0),

  METERS_PER_THIRTY_METERS("meters per thirty meters", "m/30m", Domain.DIMENSIONLESS, "m/30m",
      "meters per thirty meters", 0.0, 0.03333333333333333),

  METERS_PER_DEGREE_KELVIN("meters/degree kelvin", "m/K", Domain.LENGTH_PER_TEMPERATURE, "m/K", "meters/degree kelvin",
      0.0, 1.0),

  METER_PER_PASCAL("meter per Pascal", "m/Pa", Domain.LENGTH_PER_PRESSURE, "m/Pa", "meter per Pascal", 0.0, 1.0),

  METERS_PER_CENTIMETER("meters/ centimeter", "m/cm", Domain.DIMENSIONLESS, "m/cm", "meters/ centimeter", 0.0, 100.0),

  METERS_PER_DAY("meters/day", "m/d", Domain.VELOCITY, "m/d", "meters/day", 0.0, 1.157407407407407E-5),

  METERS_PER_HOUR("meters/hour", "m/h", Domain.VELOCITY, "m/h", "meters/hour", 0.0, 2.77777777777778E-4),

  METERS_PER_KILOGRAM("meters per kilogram", "m/kg", Domain.LENGTH_PER_MASS, "m/kg", "meters per kilogram", 0.0, 1.0),

  METERS_PER_KILOMETER("meters/kilometer", "m/km", Domain.DIMENSIONLESS, "m/km", "meters/kilometer", 0.0, 0.0010),

  METERS_PER_METER("meters/meter", "m/m", Domain.DIMENSIONLESS, "m/m", "meters/meter", 0.0, 1.0),

  METERS_PER_METER_KELVIN("meters/meter Kelvin", "m/m.K", Domain.INVERSE_TEMPERATURE, "m/m.K", "meters/meter Kelvin",
      0.0, 1.0),

  METERS_PER_CUBIC_METER("meters/cubic meter", "m/m3", Domain.INVERSE_AREA, "m/m3", "meters/cubic meter", 0.0, 1.0),

  METER_PER_MINUTE("meter per minute", "m/min", Domain.VELOCITY, "m/min", "meter per minute", 0.0, 0.016666666666666666),

  METERS_PER_MILLISECOND("meters/millisecond", "m/ms", Domain.VELOCITY, "m/ms", "meters/millisecond", 0.0, 1000.0),

  METERS_PER_SECOND("meters/second", "m/s", Domain.VELOCITY, "m/s", "meters/second", 0.0, 1.0, true),

  METERS_PER_SECOND_SQUARED("meters/second squared", "m/s2", Domain.ACCELERATION, "m/s2", "meters/second squared", 0.0,
      1.0),

  SQUARE_METERS("square meters", "m2", Domain.AREA, "m2", "square meters", 0.0, 1.0),

  SQUARE_METERS_PER_SECOND_PASCAL("square meters/second Pascal", "m2/Pa.s", Domain.MOBILITY, "m2/Pa.s",
      "square meters/second Pascal", 0.0, 1.0),

  SQUARE_METERS_PER_CUBIC_CENTIMETER("square meters/cubic centimeter", "m2/cm3", Domain.INVERSE_LENGTH, "m2/cm3",
      "square meters/cubic centimeter", 0.0, 1000000.0),

  SQUARE_METERS_PER_DAY("square meters/day", "m2/d", Domain.AREA_PER_TIME, "m2/d", "square meters/day", 0.0, 1.1574E-5),

  SQUARE_METERS_PER_DAY_KILOPASCAL("square meters/day kiloPascal", "m2/d.kPa", Domain.MOBILITY, "m2/d.kPa",
      "square meters/day kiloPascal", 0.0, 1.157407407407407E-8),

  SQUARE_METERS_PER_GRAM("square meters/gram", "m2/g", Domain.MASS_ATTENUATION_COEFFICIENT, "m2/g",
      "square meters/gram", 0.0, 1000.0),

  SQUARE_METERS_PER_HOUR("square meters/hour", "m2/h", Domain.AREA_PER_TIME, "m2/h", "square meters/hour", 0.0,
      2.77777777777778E-4),

  SQUARE_METERS_PER_KILOGRAM("square meters/kilogram", "m2/kg", Domain.MASS_ATTENUATION_COEFFICIENT, "m2/kg",
      "square meters/kilogram", 0.0, 1.0),

  SQUARE_METERS_PER_SQUARE_METER("square meters/square meter", "m2/m2", Domain.DIMENSIONLESS, "m2/m2",
      "square meters/square meter", 0.0, 1.0),

  SQUARE_METERS_PER_CUBIC_METER("square meters/cubic meter", "m2/m3", Domain.INVERSE_LENGTH, "m2/m3",
      "square meters/cubic meter", 0.0, 1.0),

  SQUARE_METERS_PER_MOL("square meters/mol", "m2/mol", Domain.CROSS_SECTION_ABSORPTION, "m2/mol", "square meters/mol",
      0.0, 1.0),

  SQUARE_METERS_PER_SECOND("square meters/second", "m2/s", Domain.AREA_PER_TIME, "m2/s", "square meters/second", 0.0,
      1.0),

  CUBIC_METERS("cubic meters", "m3", Domain.VOLUME, "m3", "cubic meters", 0.0, 1.0),

  CUBIC_METERS_AT_STD_CONDITION_0_DEG_C("cubic meters at std condition (0 deg C)", "m3(std,0C)",
      Domain.AMOUNT_OF_SUBSTANCE, "m3(std,0C)", "cubic meters at std condition (0 deg C)", 0.0, 0.0446158),

  CUBIC_METERS_AT_STD_CONDITION_15_DEG_C("cubic meters at std condition (15 deg C)", "m3(std,15C)",
      Domain.AMOUNT_OF_SUBSTANCE, "m3(std,15C)", "cubic meters at std condition (15 deg C)", 0.0, 0.0422932),

  CUBIC_METERS_PER_JOULE("cubic meters/joule", "m3/J", Domain.ISOTHERMAL_COMPRESSIBILITY, "m3/J", "cubic meters/joule",
      0.0, 1.0),

  CUBIC_METERS_PER_PASCAL("cubic meters per Pascal", "m3/Pa", Domain.VOLUME_PER_PRESSURE, "m4.s2/kg",
      "cubic meters per Pascal", 0.0, 1.0),

  CUBIC_METERS_PER_SECOND_PASCAL("cubic meters/second pascal", "m3/Pa.s", Domain.VOLUME_PER_TIME_PER_PRESSURE,
      "m3/Pa.s", "cubic meters/second pascal", 0.0, 1.0),

  CUBIC_METERS_PER_SECOND_PER_PASCAL("(cubic meters/second) per pascal", "m3/Pa/s",
      Domain.VOLUME_PER_TIME_PER_PRESSURE, "m3/Pa/s", "(cubic meters/second) per pascal", 0.0, 1.0),

  CUBIC_METERS_PER_PASCAL_SECOND_SQUARED("cubic meters/pascal second squared", "m3/Pa2.s2",
      Domain.VOLUME_PER_PASCAL_SECOND_SQUARED, "m3/Pa2.s2", "cubic meters/pascal second squared", 0.0, 1.0),

  CUBIC_METER_PER_DAY_PER_BAR("cubic meter per day per bar", "m3/bar.d", Domain.VOLUME_PER_TIME_PER_PRESSURE,
      "m3/bar.d", "cubic meter per day per bar", 0.0, 1.1574074074074074E-10),

  CUBIC_METER_PER_HOUR_PER_BAR("cubic meter per hour per bar", "m3/bar.h", Domain.VOLUME_PER_TIME_PER_PRESSURE,
      "m3/bar.h", "cubic meter per hour per bar", 0.0, 2.777777777777778E-9),

  CUBIC_METER_PER_MINUTE_PER_BAR("cubic meter per minute per bar", "m3/bar.min", Domain.VOLUME_PER_TIME_PER_PRESSURE,
      "m3/bar.min", "cubic meter per minute per bar", 0.0, 1.6666666666666668E-7),

  CUBIC_METERS_PER_CENTIPOISE_PASCAL_SECOND("cubic meters/centiPoise Pascal second", "m3/cP.Pa.s",
      Domain.VOLUME_PER_PASCAL_SECOND_SQUARED, "m3/cP.Pa.s", "cubic meters/centiPoise Pascal second", 0.0, 1000.0),

  CUBIC_METERS_PER_CENTIPOISE_DAY_KILOPASCAL("cubic meters/centiPoise day kiloPascal", "m3/cP.d.kPa",
      Domain.VOLUME_PER_PASCAL_SECOND_SQUARED, "m3/cP.d.kPa", "cubic meters/centiPoise day kiloPascal", 0.0,
      1.157407407407407E-5),

  CUBIC_METERS_PER_DAY("cubic meters/day", "m3/d", Domain.FLOWRATE, "m3/d", "cubic meters/day", 0.0,
      1.157407407407407E-5),

  CUBIC_METERS_PER_DAY_KILOPASCAL("cubic meters/day kilopascal", "m3/d.kPa", Domain.VOLUME_PER_TIME_PER_PRESSURE,
      "m3/d.kPa", "cubic meters/day kilopascal", 0.0, 1.157407407407407E-8),

  CUBIC_METER_PER_DAY_PER_METER("cubic meter per day per meter", "m3/d.m", Domain.AREA_PER_TIME, "m3/d.m",
      "cubic meter per day per meter", 0.0, 1.1574074074074073E-5),

  CUBIC_METERS_PER_DAY_PER_DAY("cubic meters/day/day", "m3/d2", Domain.VOLUME_PER_TIME_PER_TIME, "m3/d2",
      "cubic meters/day/day", 0.0, 1.339591906721536E-10),

  CUBIC_METERS_PER_GRAM("cubic meters/gram", "m3/g", Domain.VOLUME_PER_MASS, "m3/g", "cubic meters/gram", 0.0, 1000.0),

  CUBIC_METERS_PER_HOUR("cubic meters/hour", "m3/h", Domain.FLOWRATE, "m3/h", "cubic meters/hour", 0.0,
      2.77777777777778E-4),

  CUBIC_METER_PER_HOUR_PER_METER("cubic meter per hour per meter", "m3/h.m", Domain.AREA_PER_TIME, "m3/h.m",
      "cubic meter per hour per meter", 0.0, 2.777777777777778E-4),

  CUBIC_METERS_PER_HECTARE_METER("cubic meters/hectare meter", "m3/ha.m", Domain.DIMENSIONLESS, "m3/ha.m",
      "cubic meters/hectare meter", 0.0, 1.0E-4),

  CUBIC_METER_PER_DAY_PER_KILOPASCAL("cubic meter per day per kilopascal", "m3/kPa.d",
      Domain.VOLUME_PER_TIME_PER_PRESSURE, "m3/kPa.d", "cubic meter per day per kilopascal", 0.0, 1.1574074074074074E-8),

  CUBIC_METERS_PER_HOUR_PER_KILOPASCAL("(cubic meters per hour) per kilopascal", "m3/kPa.h",
      Domain.VOLUME_PER_TIME_PER_PRESSURE, "m3/kPa.h", "(cubic meters per hour) per kilopascal", 0.0,
      2.7777777777777776E-7),

  CUBIC_METERS_PER_KILOWATT_HOUR("cubic meters/kilowatt hour", "m3/kW.h", Domain.ISOTHERMAL_COMPRESSIBILITY, "m3/kW.h",
      "cubic meters/kilowatt hour", 0.0, 2.77777777777778E-7),

  CUBIC_METERS_PER_KILOGRAM("cubic meters/kilogram", "m3/kg", Domain.VOLUME_PER_MASS, "m3/kg", "cubic meters/kilogram",
      0.0, 1.0),

  CUBIC_METERS_PER_KILOMETER("cubic meters/kilometer", "m3/km", Domain.AREA, "m3/km", "cubic meters/kilometer", 0.0,
      0.0010),

  CUBIC_METERS_PER_METER("cubic meters/meter", "m3/m", Domain.AREA, "m3/m", "cubic meters/meter", 0.0, 1.0),

  CUBIC_METER_PER_SQUARE_METER("cubic meter per square meter", "m3/m2", Domain.DISTANCE, "m3/m2",
      "cubic meter per square meter", 0.0, 1.0),

  CUBIC_METERS_PER_CUBIC_METER("cubic meters/cubic meter", "m3/m3", Domain.DIMENSIONLESS, "m3/m3",
      "cubic meters/cubic meter", 0.0, 1.0),

  CUBIC_METER_PER_MINUTE("cubic meter per minute", "m3/min", Domain.FLOWRATE, "m3/min", "cubic meter per minute", 0.0,
      0.016666666666666666),

  CUBIC_METERS_PER_MOLE("cubic meters/mole", "m3/mol", Domain.MOLAR_VOLUME, "m3/mol", "cubic meters/mole", 0.0, 1.0),

  CUBIC_METERS_PER_MOLE_KILOGRAM("cubic meters/mole (kilogram)", "m3/mol(kg)", Domain.MOLAR_VOLUME, "m3/mol(kg)",
      "cubic meters/mole (kilogram)", 0.0, 1.0),

  CUBIC_METER_PER_DAY_PER_POUND_PER_SQUARE_INCH("cubic meter per day per (pound per square inch)", "m3/psi.d",
      Domain.VOLUME_PER_TIME_PER_PRESSURE, "m3/psi.d", "cubic meter per day per (pound per square inch)", 0.0,
      1.6786775914037398E-9),

  CUBIC_METERS_PER_RADIAN("cubic meters per radian", "m3/rad", Domain.VOLUME_PER_ROTATION, "m3/rad",
      "cubic meters per radian", 0.0, 1.0),

  CUBIC_METERS_PER_SECOND("cubic meters/second", "m3/s", Domain.FLOWRATE, "m3/s", "cubic meters/second", 0.0, 1.0),

  CUBIC_METER_PER_SECOND_PER_FOOT("cubic meter per second per foot", "m3/s.ft", Domain.AREA_PER_TIME, "m3/s.ft",
      "cubic meter per second per foot", 0.0, 3.280839895013123),

  CUBIC_METERS_PER_SECOND_METER("cubic meters/second meter", "m3/s.m", Domain.AREA_PER_TIME, "m3/s.m",
      "cubic meters/second meter", 0.0, 1.0),

  CUBIC_METERS_PER_SECOND_SQUARE_METER("cubic meters/second square meter", "m3/s.m2", Domain.VELOCITY, "m3/s.m2",
      "cubic meters/second square meter", 0.0, 1.0),

  CUBIC_METERS_PER_SECONDS_SQUARED("cubic meters/seconds squared", "m3/s2", Domain.VOLUME_PER_TIME_PER_TIME, "m3/s2",
      "cubic meters/seconds squared", 0.0, 1.0),

  CUBIC_METERS_PER_STD_CUBIC_METERS_AT_0_DEG_C("cubic meters/std cubic meters, 0 deg C", "m3/scm(0C)",
      Domain.CUBIC_METERS_PER_STD_CUBIC_METERS_AT_0_DEG_C, "m3/scm(0C)", "cubic meters/std cubic meters, 0 deg C", 0.0,
      1.0),

  CUBIC_METERS_PER_STD_CUBIC_METERS_AT_15_DEG_C("cubic meters/std cubic meters, 15 deg C", "m3/scm(15C)",
      Domain.VOLUME_PER_STANDARD_VOLUME, "m3/scm(15C)", "cubic meters/std cubic meters, 15 deg C", 0.0, 1.0),

  CUBIC_METERS_PER_TONNE("cubic meters/tonne", "m3/t", Domain.VOLUME_PER_MASS, "m3/t", "cubic meters/tonne", 0.0,
      0.0010),

  CUBIC_METERS_PER_UK_TON("cubic meters per UK ton", "m3/tonUK", Domain.VOLUME_PER_MASS, "m3/tonUK",
      "cubic meters per UK ton", 0.0, 9.842064392690496E-4),

  CUBIC_METERS_PER_US_TON("cubic meters per US ton", "m3/tonUS", Domain.VOLUME_PER_MASS, "m3/tonUS",
      "cubic meters per US ton", 0.0, 0.001102311359527999),

  METERS_FOURTH("meters fourth", "m4", Domain.MOMENT_OF_SECTION, "m4", "meters fourth", 0.0, 1.0),

  METERS_FOURTH_PER_SECOND("meters fourth/second", "m4/s", Domain.VOLUME_LENGTH_PER_TIME, "m4/s",
      "meters fourth/second", 0.0, 1.0),

  MILLIAMP("milliamp", "mA", Domain.ELECTRIC_CURRENT, "mA", "milliamp", 0.0, 0.0010),

  MILLIAMPERE_PER_SQUARE_CENTIMETER("milliampere per square centimeter", "mA/cm2", Domain.CURRENT_DENSITY, "mA/cm2",
      "milliampere per square centimeter", 0.0, 10.0),

  MILLIAMPERE_PER_SQUARE_FOOT("milliampere per square foot", "mA/ft2", Domain.CURRENT_DENSITY, "mA/ft2",
      "milliampere per square foot", 0.0, 0.010763910416709722),

  MILLICOULOMB("millicoulomb", "mC", Domain.ELECTRIC_CHARGE, "mC", "millicoulomb", 0.0, 0.0010),

  MILLICOULOMBS_PER_SQUARE_METER("millicoulombs/square meter", "mC/m2", Domain.VOLUME_DENSITY_OF_CHARGE, "mC/m2",
      "millicoulombs/square meter", 0.0, 0.0010),

  MILLICURIE("millicurie", "mCi", Domain.RADIOACTIVITY, "mCi", "millicurie", 0.0, 3.7E7),

  MILLIDARCIES("millidarcies", "mD", Domain.AREA, "mD", "millidarcies", 0.0, 9.86932E-16),

  MILLIDARCY_FOOT("millidarcy foot", "mD.ft", Domain.VOLUME, "mD.ft", "millidarcy foot", 0.0, 3.008141E-16),

  MILLIDARCY_SQ_FEET_PER_POUND_FORCE_SECOND("millidarcy sq feet/pound force second", "mD.ft2/lbf.s", Domain.MOBILITY,
      "mD.ft2/lbf.s", "millidarcy sq feet/pound force second", 0.0, 2.061250293962481E-17),

  MILLIDARCY_SQ_INCHES_PER_POUND_FORCE_SECOND("millidarcy sq inches/pound force second", "mD.in2/lbf.s",
      Domain.MOBILITY, "mD.in2/lbf.s", "millidarcy sq inches/pound force second", 0.0, 1.431423906600334E-19),

  MILLIDARCY_METERS("millidarcy meters", "mD.m", Domain.VOLUME, "mD.m", "millidarcy meters", 0.0, 9.86932E-16),

  MILLIDARCIES_PER_PASCAL_SECOND("millidarcies/Pascal second", "mD/Pa.s", Domain.MOBILITY, "mD/Pa.s",
      "millidarcies/Pascal second", 0.0, 9.86932E-16),

  MILLIDARCIES_PER_CENTIPOISE("millidarcies/centipoise", "mD/cP", Domain.MOBILITY, "mD/cP", "millidarcies/centipoise",
      0.0, 9.86932E-13),

  MILLIEUCLID("milliEuclid", "mEuc", Domain.DIMENSIONLESS, "mEuc", "milliEuclid", 0.0, 0.0010),

  MILLIFARAD("millifarad", "mF", Domain.CAPACITANCE, "mF", "millifarad", 0.0, 0.0010),

  MILLIGAL("milligal", "mGal", Domain.ACCELERATION, "mGal", "milligal", 0.0, 1.0E-5),

  GERMAN_LEGAL_METER("German legal meter", "mGer", Domain.DISTANCE, "mGer", "German legal meter", 0.0, 1.0000135965),

  MILLIGRAY("milligray", "mGy", Domain.ABSORBED_DOSE, "mGy", "milligray", 0.0, 0.0010),

  MILLIHENRIES("millihenries", "mH", Domain.INDUCTANCE, "mH", "millihenries", 0.0, 0.0010),

  MILLIHERTZ("millihertz", "mHz", Domain.ROTATIONAL_VELOCITY, "mHz", "millihertz", 0.0, 0.006283185307),

  MILLIJOULES("millijoules", "mJ", Domain.ENERGY, "mJ", "millijoules", 0.0, 0.0010),

  MILLIJOULES_PER_SQUARE_CENTIMETER("millijoules/square centimeter", "mJ/cm2", Domain.FORCE_PER_LENGTH, "mJ/cm2",
      "millijoules/square centimeter", 0.0, 10.0),

  MILLIJOULES_PER_SQUARE_METER("millijoules/square meter", "mJ/m2", Domain.FORCE_PER_LENGTH, "mJ/m2",
      "millijoules/square meter", 0.0, 0.0010),

  MILLIDEGREES_KELVIN_PER_METER("millidegrees Kelvin/meter", "mK/m", Domain.TEMPERATURE_PER_LENGTH, "mK/m",
      "millidegrees Kelvin/meter", 0.0, 0.0010),

  MILLILITRE("millilitre", "mL", Domain.VOLUME, "mL", "millilitre", 0.0, 1.0E-6),

  MILLILITRES_PER_UK_GALLON("millilitres/UK gallon", "mL/galUK", Domain.DIMENSIONLESS, "mL/galUK",
      "millilitres/UK gallon", 0.0, 2.199692E-4),

  MILLILITRES_PER_US_GALLON("millilitres/US gallon", "mL/galUS", Domain.DIMENSIONLESS, "mL/galUS",
      "millilitres/US gallon", 0.0, 2.64172E-4),

  MILLILITER_PER_MILLILITER("milliliter per milliliter", "mL/mL", Domain.DIMENSIONLESS, "mL/mL",
      "milliliter per milliliter", 0.0, 1.0),

  MILLIMETER("millimeter", "mm", Domain.DISTANCE, "mm", "millimeter", 0.0, 1000.0),

  MILLINEWTONS("millinewtons", "mN", Domain.FORCE, "mN", "millinewtons", 0.0, 0.0010),

  MILLINEWTON_METERS_SQUARED("millinewton meters squared", "mN.m2", Domain.FORCE_AREA, "mN.m2",
      "millinewton meters squared", 0.0, 0.0010),

  MILLINEWTONS_PER_KILOMETER("millinewtons/kilometer", "mN/km", Domain.FORCE_PER_LENGTH, "mN/km",
      "millinewtons/kilometer", 0.0, 1.0E-6),

  MILLINEWTONS_PER_METER("millinewtons/meter", "mN/m", Domain.FORCE_PER_LENGTH, "mN/m", "millinewtons/meter", 0.0,
      0.0010),

  MILLIPOISE("millipoise", "mP", Domain.DYNAMIC_VISCOSITY, "mP", "millipoise", 0.0, 1.0E-4),

  MILLIPASCAL("millipascal", "mPa", Domain.PRESSURE, "mPa", "millipascal", 0.0, 0.0010),

  MILLIPASCAL_SECONDS("millipascal seconds", "mPa.s", Domain.DYNAMIC_VISCOSITY, "mPa.s", "millipascal seconds", 0.0,
      0.0010),

  MILLISIEMEN("millisiemen", "mS", Domain.ELECTRIC_CONDUCTANCE, "mS", "millisiemen", 0.0, 0.0010),

  MILLISIEMENS_PER_METER("millisiemens/meter", "mS/m", Domain.CONDUCTIVITY, "mS/m", "millisiemens/meter", 0.0, 0.0010),

  MILLISIEVERT("millisievert", "mSv", Domain.DOSE_EQUIVALENT, "mSv", "millisievert", 0.0, 0.0010),

  MILLISIEVERTS_PER_HOUR("millisieverts per hour", "mSv/h", Domain.DOSE_EQUIVALENT_RATE, "mSv/h",
      "millisieverts per hour", 0.0, 2.7777777777777776E-7),

  MILLITESLAS("milliteslas", "mT", Domain.MAGNETIC_FLUX_DENSITY, "mT", "milliteslas", 0.0, 0.0010),

  MILLIVOLTS("millivolts", "mV", Domain.ELECTRIC_POTENTIAL, "mV", "millivolts", 0.0, 0.0010),

  MILLIVOLT_PER_FOOT("millivolt per foot", "mV/ft", Domain.ELECTRIC_POTENTIAL_PER_LENGTH, "mV/ft",
      "millivolt per foot", 0.0, 0.0032808398950131233),

  MILLIVOLT_PER_METER("millivolt per meter", "mV/m", Domain.ELECTRIC_POTENTIAL_PER_LENGTH, "mV/m",
      "millivolt per meter", 0.0, 0.0010),

  MILLIWATT("milliwatt", "mW", Domain.POWER, "mW", "milliwatt", 0.0, 0.0010),

  MILLIWATTS_PER_SQUARE_METERS("milliwatts/square meters", "mW/m2", Domain.POWER_PER_AREA, "mW/m2",
      "milliwatts/square meters", 0.0, 0.0010),

  MILLIWEBERS("milliwebers", "mWb", Domain.MAGNETIC_FLUX, "mWb", "milliwebers", 0.0, 0.0010),

  MILLIBAR("millibar", "mbar", Domain.PRESSURE, "mbar", "millibar", 0.0, 100.0),

  MILLICALORIE("millicalorie", "mcal", Domain.ENERGY, "mcal", "millicalorie", 0.0, 0.004184),

  MILLIELECTRON_VOLTS("millielectron-volts", "meV", Domain.ENERGY, "meV", "millielectron-volts", 0.0, 1.60219E-22),

  MILLIEQUIVALENT("milliequivalent", "meq", Domain.ELECTROCHEMICAL_EQUIVALENT, "meq", "milliequivalent", 0.0, 0.0010),

  MILLIEQUIVALENTS_PER_HECTOGRAM("milliequivalents/ hectogram", "meq/100g", Domain.EQUIVALENT_PER_MASS, "meq/100g",
      "milliequivalents/ hectogram", 0.0, 100.0),

  MILLIEQUIVALENTS_PER_CUBIC_CENTIMETER("milliequivalents/ cubic centimeter", "meq/cm3", Domain.EQUIVALENT_PER_VOLUME,
      "meq/cm3", "milliequivalents/ cubic centimeter", 0.0, 1000.0),

  MILLIEQUIVALENTS_PER_GRAM("milliequivalents/ gram", "meq/g", Domain.PER_MASS, "meq/g", "milliequivalents/ gram", 0.0,
      1.0),

  MILLIEQUIVALENTS_PER_MILLIGRAM("milliequivalents/ milligram", "meq/mg", Domain.EQUIVALENT_PER_MASS, "meq/mg",
      "milliequivalents/ milligram", 0.0, 0.001),

  MILLIGRAM("milligram", "mg", Domain.MASS, "mg", "milligram", 0.0, 1.0E-6),

  MILLIGRAMS_PER_JOULE("milligrams/joule", "mg/J", Domain.MASS_PER_ENERGY, "mg/J", "milligrams/joule", 0.0, 1.0E-6),

  MILLIGRAM_PER_LITRE("milligram per litre", "mg/L", Domain.MASS_PER_VOLUME, "mg/L", "milligram per litre", 0.0, 0.0010),

  MILLIGRAMS_PER_CUBIC_DECIMETER("milligrams/cubic decimeter", "mg/dm3", Domain.MASS_PER_VOLUME, "mg/dm3",
      "milligrams/cubic decimeter", 0.0, 0.0010),

  MILLIGRAMS_PER_US_GALLON("milligrams/US gallon", "mg/galUS", Domain.MASS_PER_VOLUME, "mg/galUS",
      "milligrams/US gallon", 0.0, 2.64172E-4),

  MILLIGRAMS_PER_KILOGRAM("milligrams/kilogram", "mg/kg", Domain.DIMENSIONLESS, "mg/kg", "milligrams/kilogram", 0.0,
      1.0E-6),

  MILLIGRAMS_PER_CUBIC_METER("milligrams/cubic meter", "mg/m3", Domain.MASS_PER_VOLUME, "mg/m3",
      "milligrams/cubic meter", 0.0, 1.0E-6),

  MILLIGAMMA("milligamma", "mgamma", Domain.MAGNETIC_FIELD_STRENGTH, "mgamma", "milligamma", 0.0, 7.957747E-7),

  MILLIGAUSS("milligauss", "mgauss", Domain.MAGNETIC_FLUX_DENSITY, "mgauss", "milligauss", 0.0, 1.0E-7),

  MILLIGRAVITY("milligravity", "mgn", Domain.ACCELERATION, "mgn", "milligravity", 0.0, 0.00980665),

  MHOS("mhos", "mho", Domain.ELECTRIC_CONDUCTANCE, "mho", "mhos", 0.0, 1.0),

  MHOS_PER_METER("mhos/meter", "mho/m", Domain.CONDUCTIVITY, "mho/m", "mhos/meter", 0.0, 1.0),

  MILE("mile", "mi", Domain.DISTANCE, "mi", "mile", 0.0, 1609.344),

  MILES_PER_UK_GALLON("miles/UK gallon", "mi/galUK", Domain.INVERSE_AREA, "mi/galUK", "miles/UK gallon", 0.0,
      354006.034193765),

  MILES_PER_US_GALLON("miles/US gallon", "mi/galUS", Domain.INVERSE_AREA, "mi/galUS", "miles/US gallon", 0.0,
      425143.683171079),

  MILES_PER_HOUR("miles/hour", "mi/h", Domain.VELOCITY, "mi/h", "miles/hour", 0.0, 0.44704),

  MILES_PER_FOOT("miles/foot", "mi/ft", Domain.DIMENSIONLESS, "mi/ft", "miles/foot", 0.0, 5280.0),

  MILES_PER_INCH("miles/inch", "mi/in", Domain.DIMENSIONLESS, "mi/in", "miles/inch", 0.0, 63360.0),

  SQUARE_MILES("square miles", "mi2", Domain.AREA, "mi2", "square miles", 0.0, 2589988.0),

  CUBIC_MILE("cubic mile", "mi3", Domain.VOLUME, "mi3", "cubic mile", 0.0, 4.168182E9),

  US_SURVEY_MILE("U.S. Survey mile", "miUS", Domain.DISTANCE, "miUS", "U.S. Survey mile", 0.0, 1609.3472186944373),

  US_SURVEY_SQUARE_MILE("U.S. Survey square mile", "miUS2", Domain.AREA, "miUS2", "U.S. Survey square mile", 0.0,
      2589998.0),

  MIL("mil", "mil", Domain.DISTANCE, "mil", "mil", 0.0, 2.54E-5),

  MILS_PER_YEAR("mils/year", "mil/yr", Domain.VELOCITY, "mil/yr", "mils/year", 0.0, 8.0486340295613E-13),

  MIL_6400("mil_6400", "mila", Domain.PLANE_ANGLE, "mila", "mil_6400", 0.0, 9.81747704246813E-4),

  MINUTES("minutes", "min", Domain.TIME, "min", "minutes", 0.0, 60.0),

  MINUTE_PER_FOOT("minute per foot", "min/ft", Domain.TIME_PER_LENGTH, "min/ft", "minute per foot", 0.0,
      196.85039370078738),

  MINUTE_PER_METER("minute per meter", "min/m", Domain.TIME_PER_LENGTH, "min/m", "minute per meter", 0.0, 60.0),

  MINUTES_ANGULAR("minutes angular", "mina", Domain.PLANE_ANGLE, "mina", "minutes angular", 0.0, 2.908882086657216E-4),

  MILLIMETERS("millimeters", "mm", Domain.DISTANCE, "mm", "millimeters", 0.0, 0.0010),

  MILLIMETERS_PER_YEAR("millimeters/year", "mm/a", Domain.VELOCITY, "mm/a", "millimeters/year", 0.0,
      3.16875355494539E-11),

  MILLIMETERS_PER_MILLIMETER_DEGREE_KELVIN("millimeters/millimeter degree Kelvin", "mm/mm.K",
      Domain.INVERSE_TEMPERATURE, "mm/mm.K", "millimeters/millimeter degree Kelvin", 0.0, 1.0),

  MILLIMETERS_PER_SECOND("millimeters/second", "mm/s", Domain.VELOCITY, "mm/s", "millimeters/second", 0.0, 0.0010),

  SQUARE_MILLIMETERS("square millimeters", "mm2", Domain.AREA, "mm2", "square millimeters", 0.0, 1.0E-6),

  SQUARE_MILLIMETERS_PER_SQUARE_MILLIMETER("square millimeters/square millimeter", "mm2/mm2", Domain.DIMENSIONLESS,
      "mm2/mm2", "square millimeters/square millimeter", 0.0, 1.0),

  SQUARE_MILLIMETERS_PER_SECOND("square millimeters/second", "mm2/s", Domain.AREA_PER_TIME, "mm2/s",
      "square millimeters/second", 0.0, 1.0E-6),

  CUBIC_MILLIMETERS("cubic millimeters", "mm3", Domain.VOLUME, "mm3", "cubic millimeters", 0.0, 1.0E-9),

  CUBIC_MILLIMETERS_PER_JOULE("cubic millimeters/joule", "mm3/J", Domain.ISOTHERMAL_COMPRESSIBILITY, "mm3/J",
      "cubic millimeters/joule", 0.0, 1.0E-9),

  MILLIMETERS_OF_MERCURY_AT_0_DEG_C("millimeters of Mercury at 0 deg C", "mmHg(0C)", Domain.PRESSURE, "mmHg(0C)",
      "millimeters of Mercury at 0 deg C", 0.0, 133.3224),

  MILLIMHOS_PER_METER("millimhos/meter", "mmho/m", Domain.CONDUCTIVITY, "mmho/m", "millimhos/meter", 0.0, 0.0010),

  MILLIMOLE("millimole", "mmol", Domain.AMOUNT_OF_SUBSTANCE, "mmol", "millimole", 0.0, 0.0010),

  MILLIOHM("milliohm", "mohm", Domain.ELECTRIC_RESISTANCE, "mohm", "milliohm", 0.0, 0.0010),

  MOLE("mole", "mol", Domain.AMOUNT_OF_SUBSTANCE, "mol", "mole", 0.0, 1.0),

  MOLE_GRAM("mole (gram)", "mol(g)", Domain.AMOUNT_OF_SUBSTANCE, "mol(g)", "mole (gram)", 0.0, 0.0010),

  MOLE_KILOGRAM("mole (kilogram)", "mol(kg)", Domain.AMOUNT_OF_SUBSTANCE, "mol(kg)", "mole (kilogram)", 0.0, 1.0),

  MOLES_KILOGRAM_PER_HOUR("moles (kilogram)/hour", "mol(kg)/h", Domain.MOLE_PER_TIME, "mol(kg)/h",
      "moles (kilogram)/hour", 0.0, 2.77777777777778E-4),

  MOLES_KILOGRAM_PER_CUBIC_METER("moles (kilogram)/cubic meter", "mol(kg)/m3", Domain.MOLE_PER_VOLUME, "mol(kg)/m3",
      "moles (kilogram)/cubic meter", 0.0, 1.0),

  MOLES_KILOGRAM_PER_SECOND("moles (kilogram)/second", "mol(kg)/s", Domain.MOLE_PER_TIME, "mol(kg)/s",
      "moles (kilogram)/second", 0.0, 1.0),

  MOLES_POUNDS_MASS("moles (pounds mass)", "mol(lbm)", Domain.AMOUNT_OF_SUBSTANCE, "mol(lbm)", "moles (pounds mass)",
      0.0, 0.4535924),

  MOLES_POUNDS_MASS_PER_CUBIC_FOOT("moles (pounds mass)/cubic foot", "mol(lbm)/ft3", Domain.MOLE_PER_VOLUME,
      "mol(lbm)/ft3", "moles (pounds mass)/cubic foot", 0.0, 16.01846),

  MOLES_POUNDS_MASS_PER_UK_GALLON("moles (pounds mass)/UK gallon", "mol(lbm)/galUK", Domain.MOLE_PER_VOLUME,
      "mol(lbm)/galUK", "moles (pounds mass)/UK gallon", 0.0, 99.77633),

  MOLES_POUNDS_MASS_PER_US_GALLON("moles (pounds mass)/US gallon", "mol(lbm)/galUS", Domain.MOLE_PER_VOLUME,
      "mol(lbm)/galUS", "moles (pounds mass)/US gallon", 0.0, 119.8264),

  MOLES_POUNDS_MASS_PER_HOUR("moles (pounds mass)/hour", "mol(lbm)/h", Domain.MOLE_PER_TIME, "mol(lbm)/h",
      "moles (pounds mass)/hour", 0.0, 1.259978888888889E-4),

  MOLES_POUNDS_MASS_PER_HOUR_SQUARE_FOOT("moles (pounds mass)/hour square foot", "mol(lbm)/h.ft2",
      Domain.MOLE_PER_TIME_PER_AREA, "mol(lbm)/h.ft2", "moles (pounds mass)/hour square foot", 0.0,
      0.001356229988694545),

  MOLES_POUNDS_MASS_PER_SECOND("moles (pounds mass)/second", "mol(lbm)/s", Domain.MOLE_PER_TIME, "mol(lbm)/s",
      "moles (pounds mass)/second", 0.0, 0.4535924),

  MOLES_POUNDS_MASS_PER_SECOND_SQUARE_FOOT("moles (pounds mass)/second square foot", "mol(lbm)/s.ft2",
      Domain.MOLE_PER_TIME_PER_AREA, "mol(lbm)/s.ft2", "moles (pounds mass)/second square foot", 0.0, 4.88242795930036),

  MOLES_PER_SQUARE_METER("moles/square meter", "mol/m2", Domain.MOLE_PER_AREA, "mol/m2", "moles/square meter", 0.0, 1.0),

  MOLES_PER_SQUARE_METER_SECOND("moles/square meter second", "mol/m2.s", Domain.MOLE_PER_TIME_PER_AREA, "mol/m2.s",
      "moles/square meter second", 0.0, 1.0),

  MOLES_PER_CUBIC_METER("moles/cubic meter", "mol/m3", Domain.MOLE_PER_VOLUME, "mol/m3", "moles/cubic meter", 0.0, 1.0),

  MOLES_PER_SECOND("moles/second", "mol/s", Domain.MOLE_PER_TIME, "mol/s", "moles/second", 0.0, 1.0),

  MILLIRADIAN("milliradian", "mrad", Domain.PLANE_ANGLE, "mrad", "milliradian", 0.0, 0.0010),

  MILLIRAD("millirad", "mrd", Domain.ABSORBED_DOSE, "mrd", "millirad", 0.0, 1.0E-5),

  MILLI_REM("milli-rem", "mrem", Domain.DOSE_EQUIVALENT, "mrem", "milli-rem", 0.0, 1.0E-5),

  MILLI_REMS_PER_HOUR("milli-rems per hour", "mrem/h", Domain.DOSE_EQUIVALENT_RATE, "mrem/h", "milli-rems per hour",
      0.0, 2.777777777777778E-9),

  MILLISECONDS("milliseconds", "ms", Domain.TIME, "ms", "milliseconds", 0.0, 0.0010, true),

  HALF_A_MILLISECOND("half a millisecond", "ms/2", Domain.TIME, "ms/2", "half a millisecond", 0.0, 5.0E-4),

  MILLISECONDS_PER_CENTIMETER("milliseconds/centimeter", "ms/cm", Domain.TIME_PER_LENGTH, "ms/cm",
      "milliseconds/centimeter", 0.0, 0.1),

  MILLISECOND_PER_FOOT("millisecond per foot", "ms/ft", Domain.TIME_PER_LENGTH, "ms/ft", "millisecond per foot", 0.0,
      0.0032808398950131233),

  MILLISECONDS_PER_INCH("milliseconds/inch", "ms/in", Domain.TIME_PER_LENGTH, "ms/in", "milliseconds/inch", 0.0,
      0.0393700787401575),

  MILLISECOND_PER_METER("millisecond per meter", "ms/m", Domain.TIME_PER_LENGTH, "ms/m", "millisecond per meter", 0.0,
      0.0010),

  MILLISECONDS_PER_SECOND("milliseconds/second", "ms/s", Domain.DIMENSIONLESS, "ms/s", "milliseconds/second", 0.0,
      0.0010),

  MILLISECONDS_ANGULAR("milliseconds angular", "mseca", Domain.PLANE_ANGLE, "mseca", "milliseconds angular", 0.0,
      4.84814E-9),

  MYRIANEWTON("myrianewton", "myN", Domain.FORCE, "myN", "myrianewton", 0.0, 10000.0),

  MYRIAMETER("myriameter", "mym", Domain.DISTANCE, "mym", "myriameter", 0.0, 10000.0),

  NANOAMPERE("nanoampere", "nA", Domain.ELECTRIC_CURRENT, "nA", "nanoampere", 0.0, 1.0E-9),

  API_NEUTRON_UNITS("API neutron units", "nAPI", Domain.API_NEUTRON, "nAPI", "API neutron units", 0.0, 1.0),

  NANOCOULOMB("nanocoulomb", "nC", Domain.ELECTRIC_CHARGE, "nC", "nanocoulomb", 0.0, 1.0E-9),

  NANOCURIE("nanocurie", "nCi", Domain.RADIOACTIVITY, "nCi", "nanocurie", 0.0, 37.0),

  NANOEUCLID("nanoeuclid", "nEuc", Domain.DIMENSIONLESS, "nEuc", "nanoeuclid", 0.0, 1.0E-9),

  NANOFARAD("nanofarad", "nF", Domain.CAPACITANCE, "nF", "nanofarad", 0.0, 1.0E-9),

  NANOGRAY("nanogray", "nGy", Domain.ABSORBED_DOSE, "nGy", "nanogray", 0.0, 1.0E-9),

  NANOHENRY("nanohenry", "nH", Domain.INDUCTANCE, "nH", "nanohenry", 0.0, 1.0E-9),

  NANOHERTZ("nanohertz", "nHz", Domain.ROTATIONAL_VELOCITY, "nHz", "nanohertz", 0.0, 6.283185307E-9),

  NANOJOULES("nanojoules", "nJ", Domain.ENERGY, "nJ", "nanojoules", 0.0, 1.0E-9),

  NANONEWTON("nanonewton", "nN", Domain.FORCE, "nN", "nanonewton", 0.0, 1.0E-9),

  NANOPOISE("nanopoise", "nP", Domain.DYNAMIC_VISCOSITY, "nP", "nanopoise", 0.0, 1.0E-10),

  NANOPASCAL("nanopascal", "nPa", Domain.PRESSURE, "nPa", "nanopascal", 0.0, 1.0E-9),

  NANOSIEMEN("nanosiemen", "nS", Domain.ELECTRIC_CONDUCTANCE, "nS", "nanosiemen", 0.0, 1.0E-9),

  NANOTESLAS("nanoteslas", "nT", Domain.MAGNETIC_FLUX_DENSITY, "nT", "nanoteslas", 0.0, 1.0E-9),

  NANOVOLT("nanovolt", "nV", Domain.ELECTRIC_POTENTIAL, "nV", "nanovolt", 0.0, 1.0E-9),

  NANOWATTS("nanowatts", "nW", Domain.POWER, "nW", "nanowatts", 0.0, 1.0E-9),

  NANOWEBER("nanoweber", "nWb", Domain.MAGNETIC_FLUX, "nWb", "nanoweber", 0.0, 1.0E-9),

  NANOYEAR("nanoyear", "na", Domain.TIME, "na", "nanoyear", 0.0, 0.03155815),

  NAUTICAL_MILE("nautical mile", "nautmi", Domain.DISTANCE, "nautmi", "nautical mile", 0.0, 1852.0),

  NANOCALORIE("nanocalorie", "ncal", Domain.ENERGY, "ncal", "nanocalorie", 0.0, 4.184E-9),

  NANOELECTRON_VOLTS("nanoelectron-volts", "neV", Domain.ENERGY, "neV", "nanoelectron-volts", 0.0, 1.60219E-28),

  NANOGRAM("nanogram", "ng", Domain.MASS, "ng", "nanogram", 0.0, 1.0E-12),

  NANOGAMMA("nanogamma", "ngamma", Domain.MAGNETIC_FIELD_STRENGTH, "ngamma", "nanogamma", 0.0, 7.957747E-13),

  NANOGAUSS("nanogauss", "ngauss", Domain.MAGNETIC_FLUX_DENSITY, "ngauss", "nanogauss", 0.0, 1.0E-13),

  NANOMETERS("nanometers", "nm", Domain.DISTANCE, "nm", "nanometers", 0.0, 1.0E-9),

  NANOMETER_PER_SECOND("nanometer per second", "nm/s", Domain.VELOCITY, "nm/s", "nanometer per second", 0.0, 1.0E-9),

  NANOMHO("nanomho", "nmho", Domain.ELECTRIC_CONDUCTANCE, "nmho", "nanomho", 0.0, 1.0E-9),

  NANOHM("nanohm", "nohm", Domain.ELECTRIC_RESISTANCE, "nohm", "nanohm", 0.0, 1.0E-9),

  NANORAD("nanorad", "nrd", Domain.ABSORBED_DOSE, "nrd", "nanorad", 0.0, 1.0E-11),

  NANOSECONDS("nanoseconds", "ns", Domain.TIME, "ns", "nanoseconds", 0.0, 1.0E-9),

  NANOSECONDS_PER_FOOT("nanoseconds/foot", "ns/ft", Domain.TIME_PER_LENGTH, "ns/ft", "nanoseconds/foot", 0.0,
      3.28083989501312E-9),

  NANOSECONDS_PER_METER("nanoseconds/meter", "ns/m", Domain.TIME_PER_LENGTH, "ns/m", "nanoseconds/meter", 0.0, 1.0E-9),

  NANOSECONDS_PER_SECOND("nanoseconds/second", "ns/s", Domain.DIMENSIONLESS, "ns/s", "nanoseconds/second", 0.0, 1.0E-9),

  OHM("ohm", "ohm", Domain.ELECTRIC_RESISTANCE, "ohm", "ohm", 0.0, 1.0),

  OHM_CENTIMETERS("ohm centimeters", "ohm.cm", Domain.RESISTIVITY, "ohm.cm", "ohm centimeters", 0.0, 0.01),

  OHM_METER("ohm meter", "ohmm", Domain.RESISTIVITY, "ohmm", "ohm meter", 0.0, 1.0),

  OHM_PER_METER("ohm per meter", "ohm/m", Domain.RESISTIVITY_PER_LENGTH, "ohm/m", "ohm per meter", 0.0, 1.0),

  AVOIRDUPOIS_OUNCES("avoirdupois ounces", "oz(av)", Domain.MASS, "oz(av)", "avoirdupois ounces", 0.0, 0.02834952),

  TROY_OUNCES("troy ounces", "oz(troy)", Domain.MASS, "oz(troy)", "troy ounces", 0.0, 0.03110348),

  OUNCE_FORCE("ounce force", "ozf", Domain.FORCE, "ozf", "ounce force", 0.0, 0.278013875),

  OUNCE_MASS("ounce mass", "ozm", Domain.MASS, "ozm", "ounce mass", 0.0, 0.028349525),

  PICOAMPERE("picoampere", "pA", Domain.ELECTRIC_CURRENT, "pA", "picoampere", 0.0, 1.0E-12),

  PICOCOULOMB("picocoulomb", "pC", Domain.ELECTRIC_CHARGE, "pC", "picocoulomb", 0.0, 1.0E-12),

  PICOCURIE("picocurie", "pCi", Domain.RADIOACTIVITY, "pCi", "picocurie", 0.0, 0.037),

  PICOCURIE_PER_GRAM("picocurie per gram", "pCi/g", Domain.SPECIFIC_ACTIVITY_OF_RADIOACTIVITY, "pCi/g",
      "picocurie per gram", 0.0, 37.0),

  PICOEUCLID("picoeuclid", "pEuc", Domain.DIMENSIONLESS, "pEuc", "pico euclid", 0.0, 1.0E-12),

  PICROFARADS("picrofarads", "pF", Domain.CAPACITANCE, "pF", "picrofarads", 0.0, 1.0E-12),

  PICOGRAY("picogray", "pGy", Domain.ABSORBED_DOSE, "pGy", "picogray", 0.0, 1.0E-12),

  PH("pH", "pH", Domain.ACIDITY, "pH", "pH", 0.0, 1.0),

  PICOHERTZ("picohertz", "pHz", Domain.ROTATIONAL_VELOCITY, "pHz", "picohertz", 0.0, 6.283185307E-12),

  PICOJOULE("picojoule", "pJ", Domain.ENERGY, "pJ", "picojoule", 0.0, 1.0E-12),

  PICONEWTON("piconewton", "pN", Domain.FORCE, "pN", "piconewton", 0.0, 1.0E-12),

  PICOPOISE("picopoise", "pP", Domain.DYNAMIC_VISCOSITY, "pP", "picopoise", 0.0, 1.0E-13),

  PICOPASCAL("picopascal", "pPa", Domain.PRESSURE, "pPa", "picopascal", 0.0, 1.0E-12),

  PICOSIEMENS("picosiemens", "pS", Domain.ELECTRIC_CONDUCTANCE, "pS", "picosiemens", 0.0, 1.0E-12),

  PICOTESLA("picotesla", "pT", Domain.MAGNETIC_FLUX_DENSITY, "pT", "picotesla", 0.0, 1.0E-12),

  PICOVOLT("picovolt", "pV", Domain.ELECTRIC_POTENTIAL, "pV", "picovolt", 0.0, 1.0E-12),

  PICOWATT("picowatt", "pW", Domain.POWER, "pW", "picowatt", 0.0, 1.0E-12),

  PICOWEBER("picoweber", "pWb", Domain.MAGNETIC_FLUX, "pWb", "picoweber", 0.0, 1.0E-12),

  PICOCALORIE("picocalorie", "pcal", Domain.ENERGY, "pcal", "picocalorie", 0.0, 4.184E-12),

  POUNDALS("poundals", "pdl", Domain.FORCE, "pdl", "poundals", 0.0, 0.138255),

  POUNDAL_CENTIMETER_SQUARED("poundal centimeter squared", "pdl.cm2", Domain.FORCE_AREA, "pdl.cm2",
      "poundal centimeter squared", 0.0, 1.38255E-5),

  FOOT_POUNDAL("foot poundal", "pdl.ft", Domain.ENERGY, "pdl.ft", "foot poundal", 0.0, 0.04214012),

  POUNDALS_PER_CENTIMETER("poundals/centimeter", "pdl/cm", Domain.FORCE_PER_LENGTH, "pdl/cm", "poundals/centimeter",
      0.0, 13.8255),

  PICOELECTRON_VOLTS("picoelectron-volts", "peV", Domain.ENERGY, "peV", "picoelectron-volts", 0.0, 1.60219E-31),

  PER_MILLE("per mille", "permil", Domain.DIMENSIONLESS, "permil", "per mille", 0.0, 0.0010),

  PICOGRAM("picogram", "pg", Domain.MASS, "pg", "pickgram", 0.0, 1.0E-15),

  PICOGAMMA("picogamma", "pgamma", Domain.MAGNETIC_FIELD_STRENGTH, "pgamma", "picogamma", 0.0, 7.957747E-16),

  PICOGAUSS("picogauss", "pgauss", Domain.MAGNETIC_FLUX_DENSITY, "pgauss", "picogauss", 0.0, 1.0E-16),

  PICOMETER("picometer", "pm", Domain.DISTANCE, "pm", "picometer", 0.0, 1.0E-12),

  PICOMHO("picomho", "pmho", Domain.ELECTRIC_CONDUCTANCE, "pmho", "picomho", 0.0, 1.0E-12),

  PICOOHM("picoohm", "pohm", Domain.ELECTRIC_RESISTANCE, "pohm", "picoohm", 0.0, 1.0E-12),

  PARTS_PER_TEN_THOUSAND("parts per ten thousand", "ppdk", Domain.DIMENSIONLESS, "ppdk", "parts per ten thousand", 0.0,
      1.0E-4),

  PARTS_PER_THOUSAND("parts per thousand", "ppk", Domain.DIMENSIONLESS, "ppk", "parts per thousand", 0.0, 0.0010),

  PARTS_PER_MILLION("parts per million", "ppm", Domain.DIMENSIONLESS, "ppm", "parts per million", 0.0, 1.0E-6),

  PART_PER_MILLION_PER_DEGREE_CELSIUS("part per million per degree Celsius", "ppm/degC", Domain.INVERSE_TEMPERATURE,
      "ppm/degC", "part per million per degree Celsius", 0.0, 1.0E-6),

  PART_PER_MILLION_PER_DEGREE_FAHRENHEIT("part per million per degree Fahrenheit", "ppm/degF",
      Domain.INVERSE_TEMPERATURE, "ppm/degF", "part per million per degree Fahrenheit", 0.0, 1.8E-6),

  PICORAD("picorad", "prd", Domain.ABSORBED_DOSE, "prd", "picorad", 0.0, 1.0E-14),

  PICOSECOND("picosecond", "ps", Domain.TIME, "ps", "picosecond", 0.0, 1.0E-12),

  POUNDS_PER_SQUARE_FOOT("pounds/square foot", "psf", Domain.PRESSURE, "psf", "pounds/square foot", 0.0, 47.88026),

  POUNDS_PER_SQUARE_INCH("pounds/square inch", "psi", Domain.PRESSURE, "psi", "pounds/square inch", 0.0, 6894.757),

  POUNDS_PER_SQUARE_INCH_DAYS_PER_BARREL("pounds per square inch days/barrel", "psi.d/bbl",
      Domain.PRESSURE_TIME_PER_VOLUME, "psi.d/bbl", "pounds per square inch days/barrel", 0.0, 0.0433667141387631),

  POUND_PER_SQUARE_INCH_SECOND("pound per square inch second", "psi.s", Domain.DYNAMIC_VISCOSITY, "psi.s",
      "pound per square inch second", 0.0, 6894.757),

  POUNDS_PER_SQUARE_INCH_PER_100_FEET("pounds/square inch per 100 feet", "psi/100ft", Domain.FORCE_PER_VOLUME,
      "psi/100ft", "pounds/square inch per 100 feet", 0.0, 226.20593832020995),

  POUNDS_PER_SQUARE_INCH_PER_FOOT("pounds/square inch per foot", "psi/ft", Domain.PRESSURE_PER_LENGTH, "psi/ft",
      "pounds/square inch per foot", 0.0, 22620.593832021),

  POUND_PER_SQUARE_INCH_PER_HOUR("pound per square inch per hour", "psi/h", Domain.PRESSURE_PER_TIME, "psi/h",
      "pound per square inch per hour", 0.0, 1.9152102777777777),

  POUNDS_PER_SQUARE_INCH_PER_THOUSAND_FEET("pounds/square inch per thousand feet", "psi/kft", Domain.FORCE_PER_VOLUME,
      "psi/kft", "pounds/square inch per thousand feet", 0.0, 22.620593832020994),

  POUND_PER_SQUARE_INCH_PER_METER("pound per square inch per meter", "psi/m", Domain.FORCE_PER_VOLUME, "psi/m",
      "pound per square inch per meter", 0.0, 6894.757),

  POUND_PER_SQUARE_INCH_PER_MINUTE("pound per square inch per minute", "psi/min", Domain.PRESSURE_PER_TIME, "psi/min",
      "pound per square inch per minute", 0.0, 114.91261666666666),

  POUND_PER_SQUARE_INCH_SQUARED("pound per square inch squared", "psi2", Domain.PRESSURE_SQUARED, "psi2",
      "pound per square inch squared", 0.0, 4.7537674E7),

  PSI_SQUARED_DAYS_PER_CENTIPOISE_CUBIC_FOOT("psi squared days/ centipoise cubic foot", "psi2.d/cP.ft3",
      Domain.DARCY_FLOW_COEFFICIENT, "psi2.d/cP.ft3", "psi squared days/ centipoise cubic foot", 0.0,
      1.45046340833568064E17),

  PSI_DAYS_PER_CUBIC_FOOT_SQUARED_PER_CENTIPOISE("(psi days/cubic foot)squared/centipoise", "psi2.d2/cP.ft6",
      Domain.NON_DARCY_FLOW_COEFFICIENT, "psi2.d2/cP.ft6", "(psi days/cubic foot)squared/centipoise", 0.0,
      4.425637306792171E23),

  POUNDS_PER_SQUARE_INCH_SQUARED_PER_CENTIPOISE("pounds/square inch squared/ centipoise", "psi2/cP",
      Domain.PRESSURE_PER_TIME, "psi2/cP", "pounds/square inch squared/ centipoise", 0.0, 4.75376781316982E10),

  POUNDS_PER_SQUARE_INCH_ABSOLUTE("pounds/square inch absolute", "psia", Domain.PRESSURE, "psia",
      "pounds/square inch absolute", 0.0, 6894.757),

  POUNDS_PER_SQUARE_INCH_GAUGE("pounds/square inch gauge", "psig", Domain.PRESSURE, "psig", "pounds/square inch gauge",
      101325.0, 6894.757),

  UK_PINT("UK pint", "ptUK", Domain.VOLUME, "ptUK", "UK pint", 0.0, 5.682615E-4),

  UK_PINTS_PER_1000_BARRELS("UK pints/1000 barrels", "ptUK/Mbbl", Domain.DIMENSIONLESS, "ptUK/Mbbl",
      "UK pints/1000 barrels", 0.0, 3.574253E-6),

  UK_PINTS_PER_HORSEPOWER_HOUR("UK pints/horsepower hour", "ptUK/hp.hr", Domain.ISOTHERMAL_COMPRESSIBILITY,
      "ptUK/hp.hr", "UK pints/horsepower hour", 0.0, 2.116809E-10),

  US_PINTS("US pints", "ptUS", Domain.VOLUME, "ptUS", "US pints", 0.0, 4.731765E-4),

  US_PINT_PER_TEN_BARREL("US pint per ten barrel", "ptUS/10bbl", Domain.DIMENSIONLESS, "ptUS/10bbl",
      "US pint per ten barrel", 0.0, 29.761905510691737),

  UK_QUARTS("UK quarts", "qtUK", Domain.VOLUME, "qtUK", "UK quarts", 0.0, 0.001136523),

  US_QUARTS("US quarts", "qtUS", Domain.VOLUME, "qtUS", "US quarts", 0.0, 9.463529E-4),

  QUADS("quads", "quad", Domain.ENERGY, "quad", "quads", 0.0, 1.055056E18),

  QUADS_PER_YEAR("quads/year", "quad/yr", Domain.POWER, "quad/yr", "quads/year", 0.0, 3.34321245066647E10),

  RADIAN("radian", "rad", Domain.PLANE_ANGLE, "rad", "radian", 0.0, 1.0),

  RADIANS_PER_FOOT("radians per foot", "rad/ft", Domain.ANGLE_PER_LENGTH, "rad/ft", "radians per foot", 0.0,
      3.280839895013123),

  RADIANS_PER_CUBIC_FOOT("radians per cubic foot", "rad/ft3", Domain.ANGLE_PER_VOLUME, "rad/ft3",
      "radians per cubic foot", 0.0, 35.31466672148859),

  RADIANS_PER_METER("radians/meter", "rad/m", Domain.ANGLE_PER_LENGTH, "rad/m", "radians/meter", 0.0, 1.0),

  RADIANS_PER_CUBIC_METER("radians per cubic meter", "rad/m3", Domain.ANGLE_PER_VOLUME, "rad/m3",
      "radians per cubic meter", 0.0, 1.0),

  RADIANS_PER_SECOND("radians/second", "rad/s", Domain.ROTATIONAL_VELOCITY, "rad/s", "radians/second", 0.0, 1.0),

  RADIANS_PER_SECOND_SQUARED("radians/second squared", "rad/s2", Domain.ANGULAR_ACCELERATION, "rad/s2",
      "radians/second squared", 0.0, 1.0),

  RAD("rad", "rd", Domain.ABSORBED_DOSE, "rd", "rad", 0.0, 0.01),

  REM("rem", "rem", Domain.DOSE_EQUIVALENT, "rem", "rem", 0.0, 0.01),

  REMS_PER_HOUR("rems per hour", "rem/h", Domain.DOSE_EQUIVALENT_RATE, "rem/h", "rems per hour", 0.0,
      2.777777777777778E-6),

  REVOLUTIONS_PER_MINUTE("revolutions/minute", "rev/min", Domain.ROTATIONAL_VELOCITY, "rev/min", "revolutions/minute",
      0.0, 0.1047197551166667),

  REVOLUTIONS_PER_SECOND("revolutions/second", "rev/s", Domain.ROTATIONAL_VELOCITY, "rev/s", "revolutions/second", 0.0,
      6.283185307),

  REVOLUTIONS_PER_MINUTE_PER_SECOND("revolutions/minute per second", "rpm/s", Domain.ANGULAR_ACCELERATION, "rpm/s",
      "revolutions/minute per second", 0.0, 0.1047197551166667),

  SECOND("second", "s", Domain.TIME, "s", "second", 0.0, 1.0, true),

  SECOND_PER_LITRE("second per litre", "s/L", Domain.TIME_PER_VOLUME, "s/L", "second per litre", 0.0, 1000.0),

  SECONDS_PER_CENTIMETER("seconds/centimeter", "s/cm", Domain.TIME_PER_LENGTH, "s/cm", "seconds/centimeter", 0.0, 100.0),

  SECONDS_PER_FOOT("seconds/foot", "s/ft", Domain.TIME_PER_LENGTH, "s/ft", "seconds/foot", 0.0, 3.28083989501312),

  SECOND_PER_CUBIC_FOOT("second per cubic foot", "s/ft3", Domain.TIME_PER_VOLUME, "s/ft3", "second per cubic foot",
      0.0, 35.31466672148859),

  SECONDS_PER_INCH("seconds/inch", "s/in", Domain.TIME_PER_LENGTH, "s/in", "seconds/inch", 0.0, 39.3700787401575),

  SECONDS_PER_KILOGRAM("seconds per kilogram", "s/kg", Domain.TIME_PER_MASS, "s/kg", "seconds per kilogram", 0.0, 1.0),

  SECONDS_PER_METER("seconds/meter", "s/m", Domain.TIME_PER_LENGTH, "s/m", "seconds/meter", 0.0, 1.0),

  SECONDS_PER_CUBIC_METER("seconds/cubic meter", "s/m3", Domain.TIME_PER_VOLUME, "s/m3", "seconds/cubic meter", 0.0,
      1.0),

  SECOND_PER_UK_QUART("second per UK quart", "s/qtUK", Domain.TIME_PER_VOLUME, "s/qtUK", "second per UK quart", 0.0,
      879.8766061047598),

  SECOND_PER_US_QUART("second per US quart", "s/qtUS", Domain.TIME_PER_VOLUME, "s/qtUS", "second per US quart", 0.0,
      1056.6882607957348),

  SACKS("sacks", "sack94", Domain.MASS, "sack94", "sacks", 0.0, 42.63769),

  STD_CUBIC_FEET_AT_60_DEG_F("std cubic feet at 60 deg F", "scf(60F)", Domain.STANDARD_VOLUME, "scf(60F)",
      "std cubic feet at 60 deg F", 0.0, 0.028262357),

  STD_CUBIC_FEET_AT_60_DEG_F_PER_BARREL("std cubic feet at 60 deg F/barrel", "scf(60F)/bbl",
      Domain.STANDARD_VOLUME_PER_VOLUME, "scf(60F)/bbl", "std cubic feet at 60 deg F/barrel", 0.0, 0.1777648717853564),

  STANDARD_CUBIC_FEET_PER_DAY("standard cubic feet/day", "scf(60F)/d", Domain.STANDARD_VOLUME_PER_TIME, "scf(60F)/d",
      "standard cubic feet/day", 0.0, 3.27110613425926E-7),

  STD_CUBIC_FEET_AT_60_DEG_F_PER_SQUARE_FOOT("std cubic feet at 60 deg F/square foot", "scf(60F)/ft2",
      Domain.STANDARD_VOLUME_PER_AREA, "scf(60F)/ft2", "std cubic feet at 60 deg F/square foot", 0.0, 0.304213478913069),

  STD_CUBIC_FEET_AT_60_DEG_FT_PER_CUBIC_FOOT("std cubic feet at 60 deg Ft/cubic foot", "scf(60F)/ft3",
      Domain.STANDARD_VOLUME_PER_VOLUME, "scf(60F)/ft3", "std cubic feet at 60 deg Ft/cubic foot", 0.0, 0.9980757),

  STANDARD_CUBIC_METERS_AT_0_DEG_CELSIUS("standard cubic meters at 0 deg Celsius", "scm(0C)",
      Domain.STANDARD_CUBIC_METERS_AT_0_DEG_CELSIUS, "scm(0C)", "standard cubic meters at 0 deg Celsius", 0.0, 1.0),

  STD_CUBIC_METERS_AT_0_DEG_C_PER_SQUARE_METER("std cubic meters, 0 deg C/square meter", "scm(0C)/m2",
      Domain.STD_CUBIC_METERS_AT_0_DEG_C_PER_SQUARE_METER, "scm(0C)/m2", "std cubic meters, 0 deg C/square meter", 0.0,
      1.0),

  STD_CUBIC_METERS_AT_0_DEG_C_PER_CUBIC_METER("std cubic meters, 0 deg C/cubic meter", "scm(0C)/m3",
      Domain.STD_CUBIC_METERS_AT_0_DEG_C_PER_CUBIC_METER, "scm(0C)/m3", "std cubic meters, 0 deg C/cubic meter", 0.0,
      1.0),

  STANDARD_CUBIC_METERS_AT_15_DEG_CELSIUS("standard cubic meters at 15 deg Celsius", "scm(15C)",
      Domain.STANDARD_VOLUME, "scm(15C)", "standard cubic meters at 15 deg Celsius", 0.0, 1.0),

  STD_CUBIC_METERS_AT_15_DEG_C_PER_DAY("std cubic meters at 15 deg C/day", "scm(15C)/d",
      Domain.STANDARD_VOLUME_PER_TIME, "scm(15C)/d", "std cubic meters at 15 deg C/day", 0.0, 1.157407407407407E-5),

  STD_CUBIC_METERS_AT_15_DEG_C_PER_SQUARE_METER("std cubic meters, 15 deg C/square meter", "scm(15C)/m2",
      Domain.STANDARD_VOLUME_PER_AREA, "scm(15C)/m2", "std cubic meters, 15 deg C/square meter", 0.0, 1.0),

  STD_CUBIC_METERS_AT_15_DEG_C_PER_CUBIC_METER("std cubic meters, 15 deg C/cubic meter", "scm(15C)/m3",
      Domain.STANDARD_VOLUME_PER_VOLUME, "scm(15C)/m3", "std cubic meters, 15 deg C/cubic meter", 0.0, 1.0),

  STD_CUBIC_METERS_AT_15_DEG_C_PER_SECOND("std cubic meters, 15 deg C/second", "scm(15C)/s",
      Domain.STANDARD_VOLUME_PER_TIME, "scm(15C)/s", "std cubic meters, 15 deg C/second", 0.0, 1.0),

  STD_CUBIC_METERS_PER_STOCK_TANK_BARREL("std cubic meters / stock tank barrel", "scm15/stb60", Domain.DIMENSIONLESS,
      "scm15/stb60", "std cubic meters / stock tank barrel", 0.0, 6.28981056977507),

  SECONDS_ANGULAR("seconds angular", "seca", Domain.PLANE_ANGLE, "seca", "seconds angular", 0.0, 4.84813681109536E-6),

  SECTION("section", "section", Domain.AREA, "section", "section", 0.0, 2589998.0),

  STANDARD_METER("standard meter", "sm", Domain.DISTANCE, "sm", "standard meter", 0.0, 1.0),

  STD_CUBIC_METERS_PER_1000_STD_CUBIC_METER("std cubic meters/ 1000 std cubic meter", "sm3/ksm3", Domain.DIMENSIONLESS,
      "sm3/ksm3", "std cubic meters/ 1000 std cubic meter", 0.0, 0.0010),

  STD_CUBIC_METERS_PER_STD_CUBIC_METERS("std cubic meters/ std cubic meters", "sm3/sm3", Domain.DIMENSIONLESS,
      "sm3/sm3", "std cubic meters/ std cubic meters", 0.0, 1.0),

  SQUARE_FEET("square feet", "sq ft", Domain.AREA, "sq ft", "square feet", 0.0, 0.09290304),

  SQUARE_YARDS("square yards", "sq yd", Domain.AREA, "sq yd", "square yards", 0.0, 0.8361274),

  STERADIAN("steradian", "sr", Domain.SOLID_ANGLE, "sr", "steradian", 0.0, 1.0),

  STOCK_TANK_BARREL_AT_60_DEG_F("stock tank barrel at 60 deg F", "stb(60F)", Domain.STANDARD_VOLUME, "stb(60F)",
      "stock tank barrel at 60 deg F", 0.0, 0.1589873),

  STOCK_TANK_BARRELS_AT_60_DEG_F_PER_ACRE("stock tank barrels, 60 deg F/acre", "stb(60F)/acre",
      Domain.STANDARD_VOLUME_PER_AREA, "stb(60F)/acre", "stock tank barrels, 60 deg F/acre", 0.0, 3.92864564813376E-5),

  STOCK_TANK_BARRELS_AT_60_DEG_F_PER_BARREL("stock tank barrels, 60 deg F/barrel", "stb(60F)/bbl",
      Domain.STANDARD_VOLUME_PER_VOLUME, "stb(60F)/bbl", "stock tank barrels, 60 deg F/barrel", 0.0, 1.0),

  STOCK_TANK_BARRELS_AT_60_DEG_F_PER_DAY("stock tank barrels, 60 deg F/day", "stb(60F)/d",
      Domain.STANDARD_VOLUME_PER_TIME, "stb(60F)/d", "stock tank barrels, 60 deg F/day", 0.0, 1.840130787037037E-6),

  STOCK_TANK_BARRELS_PER_MILLION_STD_CU_FT("stock tank barrels/ million std cu ft", "stb60/MMscf60",
      Domain.DIMENSIONLESS, "stb60/MMscf60", "stock tank barrels/ million std cu ft", 0.0, 5.62540838331354E-6),

  STOCK_TANK_BARRELS_PER_MILLION_STD_CU_MTS("stock tank barrels/ million std cu mts", "stb60/MMscm15",
      Domain.DIMENSIONLESS, "stb60/MMscm15", "stock tank barrels/ million std cu mts", 0.0, 1.589873E-7),

  STOCK_TANK_BARRELS_PER_1000_STD_CU_FT("stock tank barrels/ 1000 std cu ft", "stb60/Mscf60", Domain.DIMENSIONLESS,
      "stb60/Mscf60", "stock tank barrels/ 1000 std cu ft", 0.0, 0.00562540838331354),

  STOCK_TANK_BARRELS_PER_1000_STD_CU_METERS("stock tank barrels/ 1000 std cu meters", "stb60/Mscm15",
      Domain.DIMENSIONLESS, "stb60/Mscm15", "stock tank barrels/ 1000 std cu meters", 0.0, 1.589873E-4),

  STOCK_TANK_BARRELS_PER_STD_CU_METERS("stock tank barrels/ std cu meters", "stb60/scm15", Domain.DIMENSIONLESS,
      "stb60/scm15", "stock tank barrels/ std cu meters", 0.0, 0.1589873),

  TONNE("tonne", "t", Domain.MASS, "t", "tonne", 0.0, 1000.0),

  TONNES_PER_YEAR("tonnes/year", "t/a", Domain.MASS_PER_TIME, "t/a", "tonnes/year", 0.0, 3.16875355494539E-5),

  TONNES_PER_DAY("tonnes/day", "t/d", Domain.MASS_PER_TIME, "t/d", "tonnes/day", 0.0, 0.01157407407407407),

  TONNES_PER_HOUR("tonnes/hour", "t/h", Domain.MASS_PER_TIME, "t/h", "tonnes/hour", 0.0, 0.277777777777778),

  TONNES_PER_MINUTE("tonnes per minute", "t/min", Domain.MASS_PER_TIME, "t/min", "tonnes per minute", 0.0,
      16.666666666666668),

  TALBOT("talbot", "talbot", Domain.QUANTITY_OF_LIGHT, "talbot", "talbot", 0.0, 1.0),

  TRILLION_CUBIC_FEET("trillion cubic feet", "tcf", Domain.VOLUME, "tcf", "trillion cubic feet", 0.0, 2.831685E10),

  THERMS("therms", "therm", Domain.ENERGY, "therm", "therms", 0.0, 1.055056E8),

  THERMS_PER_CUBIC_FOOT("therms/cubic foot", "therm/ft3", Domain.MODULUS_OF_COMPRESSION, "therm/ft3",
      "therms/cubic foot", 0.0, 3.72589465283038E9),

  THERMS_PER_UK_GALLON("therms/UK gallon", "therm/galUK", Domain.MODULUS_OF_COMPRESSION, "therm/galUK",
      "therms/UK gallon", 0.0, 2.3208E10),

  THERMS_PER_POUND_MASS("therms/pound mass", "therm/lbm", Domain.SPECIFIC_ENERGY, "therm/lbm", "therms/pound mass",
      0.0, 2.3260001710787E8),

  TONS_OF_REFRIGERATION("tons of refrigeration", "ton of refrig", Domain.POWER, "ton of refrig",
      "tons of refrigeration", 0.0, 3516.853),

  UK_TONS("UK tons", "tonUK", Domain.MASS, "tonUK", "UK tons", 0.0, 1016.047),

  UK_TONS_PER_YEAR("UK tons/year", "tonUK/a", Domain.MASS_PER_TIME, "tonUK/a", "UK tons/year", 0.0, 3.2196025432416E-5),

  UK_TONS_PER_DAY("UK tons/day", "tonUK/d", Domain.MASS_PER_TIME, "tonUK/d", "UK tons/day", 0.0, 0.01175980324074074),

  UK_TONS_PER_HOUR("UK tons/hour", "tonUK/h", Domain.MASS_PER_TIME, "tonUK/h", "UK tons/hour", 0.0, 0.282235277777778),

  UK_TONS_PER_MINUTE("UK tons/minute", "tonUK/min", Domain.MASS_PER_TIME, "tonUK/min", "UK tons/minute", 0.0,
      16.93411666666667),

  US_TONS("US tons", "tonUS", Domain.MASS, "tonUS", "US tons", 0.0, 907.1847),

  US_TONS_PER_YEAR("US tons/year", "tonUS/a", Domain.MASS_PER_TIME, "tonUS/a", "US tons/year", 0.0, 2.87464474311707E-5),

  US_TONS_PER_DAY("US tons/day", "tonUS/d", Domain.MASS_PER_TIME, "tonUS/d", "US tons/day", 0.0, 0.01049982291666667),

  US_TONS_PER_SQUARE_FOOT("US tons/square foot", "tonUS/ft2", Domain.MASS_PER_AREA, "tonUS/ft2", "US tons/square foot",
      0.0, 9764.855),

  US_TONS_PER_HOUR("US tons/hour", "tonUS/h", Domain.MASS_PER_TIME, "tonUS/h", "US tons/hour", 0.0, 0.25199575),

  US_TONS_PER_MINUTE("US tons/minute", "tonUS/min", Domain.MASS_PER_TIME, "tonUS/min", "US tons/minute", 0.0, 15.119745),

  UK_TONS_FORCE("UK tons force", "tonfUK", Domain.FORCE, "tonfUK", "UK tons force", 0.0, 9964.016),

  UK_TON_FEET_SQUARED("UK ton feet squared", "tonfUK.ft2", Domain.FORCE_AREA, "tonfUK.ft2", "UK ton feet squared", 0.0,
      925.6874),

  UK_TONS_FORCE_PER_FOOT("UK tons force/foot", "tonfUK/ft", Domain.FORCE_PER_LENGTH, "tonfUK/ft", "UK tons force/foot",
      0.0, 32690.3412073491),

  UK_TONS_FORCE_PER_SQUARE_FOOT("UK tons force/square foot", "tonfUK/ft2", Domain.PRESSURE, "tonfUK/ft2",
      "UK tons force/square foot", 0.0, 107251.77561466233),

  US_TONS_FORCE("US tons force", "tonfUS", Domain.FORCE, "tonfUS", "US tons force", 0.0, 8896.443),

  US_TONS_FORCE_FEET("US tons force feet", "tonfUS.ft", Domain.ENERGY, "tonfUS.ft", "US tons force feet", 0.0, 2711.636),

  US_TONS_FORCE_FEET_SQUARED("US tons force feet squared", "tonfUS.ft2", Domain.FORCE_AREA, "tonfUS.ft2",
      "US tons force feet squared", 0.0, 826.5067),

  US_TONS_FORCE_PER_MILE("US tons force/mile", "tonfUS.mi", Domain.ENERGY, "tonfUS.mi", "US tons force/mile", 0.0,
      1.431744E7),

  US_TON_FORCE_MILES_PER_BARREL("US ton force miles/barrel", "tonfUS.mi/bbl", Domain.MODULUS_OF_COMPRESSION,
      "tonfUS.mi/bbl", "US ton force miles/barrel", 0.0, 9.00539854441204E7),

  US_TONS_FORCE_MILES_PER_FOOT("US tons force miles/foot", "tonfUS.mi/ft", Domain.FORCE, "tonfUS.mi/ft",
      "US tons force miles/foot", 0.0, 4.69732283464567E7),

  US_TONS_FORCE_PER_FOOT("US tons force/foot", "tonfUS/ft", Domain.FORCE_PER_LENGTH, "tonfUS/ft", "US tons force/foot",
      0.0, 29187.8051181102),

  US_TONS_FORCE_PER_SQUARE_FOOT("US tons force/square foot", "tonfUS/ft2", Domain.PRESSURE, "tonfUS/ft2",
      "US tons force/square foot", 0.0, 95760.52),

  US_TONS_FORCE_PER_SQUARE_INCH("US tons force/square inch", "tonfUS/in2", Domain.PRESSURE, "tonfUS/in2",
      "US tons force/square inch", 0.0, 1.378951E7),

  TORR("torr", "torr", Domain.PRESSURE, "torr", "torr", 0.0, 133.3224),

  MICROAMPERE("microampere", "uA", Domain.ELECTRIC_CURRENT, "uA", "microampere", 0.0, 1.0E-6),

  MICROAMPERE_PER_SQUARE_CENTIMETER("microampere per square centimeter", "uA/cm2", Domain.CURRENT_DENSITY, "uA/cm2",
      "microampere per square centimeter", 0.0, 0.01),

  MICROAMPERE_PER_SQUARE_INCH("microampere per square inch", "uA/in2", Domain.CURRENT_DENSITY, "uA/in2",
      "microampere per square inch", 0.0, 0.0015500031000062),

  MICROCOULOMB("microcoulomb", "uC", Domain.ELECTRIC_CHARGE, "uC", "microcoulomb", 0.0, 1.0E-6),

  MICROCURIE("microcurie", "uCi", Domain.RADIOACTIVITY, "uCi", "microcurie", 0.0, 37000.0),

  MICROEUCLIDS("microEuclids", "uEuc", Domain.DIMENSIONLESS, "uEuc", "microEuclids", 0.0, 1.0E-6),

  MICROFARADS("microfarads", "uF", Domain.CAPACITANCE, "uF", "microfarads", 0.0, 1.0E-6),

  MICROFARADS_PER_METER("microfarads/meter", "uF/m", Domain.CAPACITANCE_PER_LENGTH, "uF/m", "microfarads/meter", 0.0,
      1.0E-6),

  MICROGRAY("microgray", "uGy", Domain.ABSORBED_DOSE, "uGy", "microgray", 0.0, 1.0E-6),

  MICROHENRY("microhenry", "uH", Domain.INDUCTANCE, "uH", "microhenry", 0.0, 1.0E-6),

  MICROHENRIES_PER_METER("microhenries/meter", "uH/m", Domain.MAGNETIC_PERMEABILITY, "uH/m", "microhenries/meter", 0.0,
      1.0E-6),

  MICROHERTZ("microhertz", "uHz", Domain.ROTATIONAL_VELOCITY, "uHz", "microhertz", 0.0, 6.283185307E-6),

  MICROJOULES("microjoules", "uJ", Domain.ENERGY, "uJ", "microjoules", 0.0, 1.0E-6),

  MICRONEWTONS("micronewtons", "uN", Domain.FORCE, "uN", "micronewtons", 0.0, 1.0E-6),

  MICROPOISE("micropoise", "uP", Domain.DYNAMIC_VISCOSITY, "uP", "micropoise", 0.0, 1.0E-7),

  MICROPASCAL("micropascal", "uPa", Domain.PRESSURE, "uPa", "micropascal", 0.0, 1.0E-6),

  MICROSIEMENS("microsiemens", "uS", Domain.ELECTRIC_CONDUCTANCE, "uS", "microsiemens", 0.0, 1.0E-6),

  MICROTESLAS("microteslas", "uT", Domain.MAGNETIC_FLUX_DENSITY, "uT", "microteslas", 0.0, 1.0E-6),

  MICROVOLTS("microvolts", "uV", Domain.ELECTRIC_POTENTIAL, "uV", "microvolts", 0.0, 1.0E-6),

  MICROVOLT_PER_FOOT("microvolt per foot", "uV/ft", Domain.ELECTRIC_POTENTIAL_PER_LENGTH, "uV/ft",
      "microvolt per foot", 0.0, 3.280839895013123E-6),

  MICROVOLT_PER_METER("microvolt per meter", "uV/m", Domain.ELECTRIC_POTENTIAL_PER_LENGTH, "uV/m",
      "microvolt per meter", 0.0, 1.0E-6),

  MICROWATTS("microwatts", "uW", Domain.POWER, "uW", "microwatts", 0.0, 1.0E-6),

  MICROWATTS_PER_CUBIC_METER("microwatts/cubic meter", "uW/m3", Domain.POWER_PER_VOLUME, "uW/m3",
      "microwatts/cubic meter", 0.0, 1.0E-6),

  MICROWEBERS("microwebers", "uWb", Domain.MAGNETIC_FLUX, "uWb", "microwebers", 0.0, 1.0E-6),

  MICROBARS("microbars", "ubar", Domain.PRESSURE, "ubar", "microbars", 0.0, 0.1),

  MICROCALORIE("microcalorie", "ucal", Domain.ENERGY, "ucal", "microcalorie", 0.0, 4.184E-6),

  MICROCALORIES_PER_SECOND("microcalories/second", "ucal/s", Domain.POWER, "ucal/s", "microcalories/second", 0.0,
      4.1839992E-6),

  MICROCALORIES_PER_SECOND_SQUARE_CENTIMETER("microcalories/second square centimeter", "ucal/s.cm2",
      Domain.POWER_PER_AREA, "ucal/s.cm2", "microcalories/second square centimeter", 0.0, 0.04184),

  MICROELECTRON_VOLTS("microelectron-volts", "ueV", Domain.ENERGY, "ueV", "microelectron-volts", 0.0, 1.60219E-25),

  MICROGRAMS("micrograms", "ug", Domain.MASS, "ug", "micrograms", 0.0, 1.0E-9),

  MICROGRAMS_PER_CUBIC_CENTIMETER("micrograms/cubic centimeter", "ug/cm3", Domain.MASS_PER_VOLUME, "ug/cm3",
      "micrograms/cubic centimeter", 0.0, 0.0010),

  MILLIONTH_US_GALLONS("millionth US gallons", "ugalUS", Domain.VOLUME, "ugalUS", "millionth US gallons", 0.0,
      3.785412E-9),

  MICROGAMMA("microgamma", "ugamma", Domain.MAGNETIC_FIELD_STRENGTH, "ugamma", "microgamma", 0.0, 7.957747E-10),

  MICROGAUSS("microgauss", "ugauss", Domain.MAGNETIC_FLUX_DENSITY, "ugauss", "microgauss", 0.0, 1.0E-10),

  MICRONS("microns", "um", Domain.DISTANCE, "um", "microns", 0.0, 1.0E-6),

  MICROMETER_PER_SECOND("micrometer per second", "um/s", Domain.VELOCITY, "um/s", "micrometer per second", 0.0, 1.0E-6),

  SQUARE_MICRONS("square microns", "um2", Domain.AREA, "um2", "square microns", 0.0, 1.0E-12),

  SQUARE_MICRON_METERS("square micron meters", "um2.m", Domain.VOLUME, "um2.m", "square micron meters", 0.0, 1.0E-12),

  MICRONS_OF_MERCURY_AT_0_DEG_C("microns of Mercury at 0 deg C", "umHg(0C)", Domain.PRESSURE, "umHg(0C)",
      "microns of Mercury at 0 deg C", 0.0, 0.1333224),

  MICROMOLE("micromole", "umol", Domain.AMOUNT_OF_SUBSTANCE, "umol", "micromole", 0.0, 1.0E-6),

  MICROOHM("microohm", "uohm", Domain.ELECTRIC_RESISTANCE, "uohm", "microohm", 0.0, 1.0E-6),

  MICROHM_PER_FOOT("microhm per foot", "uohm/ft", Domain.RESISTIVITY_PER_LENGTH, "uohm/ft", "microhm per foot", 0.0,
      3.280839895013123E-6),

  MICROHM_PER_METER("microhm per meter", "uohm/m", Domain.RESISTIVITY_PER_LENGTH, "uohm/m", "microhm per meter", 0.0,
      1.0E-6),

  MICROPOUNDS_PER_SQUARE_INCH("micropounds/square inch", "upsi", Domain.PRESSURE, "upsi", "micropounds/square inch",
      0.0, 0.006894757),

  MICRORADIAN("microradian", "urad", Domain.PLANE_ANGLE, "urad", "microradian", 0.0, 1.0E-6),

  MICRORAD("microrad", "urd", Domain.ABSORBED_DOSE, "urd", "microrad", 0.0, 1.0E-8),

  MICROSECOND("microsecond", "us", Domain.TIME, "us", "microsecond", 0.0, 1.0E-6),

  MICROSECONDS_PER_FOOT("microseconds/foot", "us/ft", Domain.TIME_PER_LENGTH, "us/ft", "microseconds/foot", 0.0,
      3.28083989501312E-6),

  MICROSECONDS_PER_METER("microseconds/meter", "us/m", Domain.TIME_PER_LENGTH, "us/m", "microseconds/meter", 0.0,
      1.0E-6),

  MICROSECONDS_PER_SECOND("microseconds/second", "us/s", Domain.DIMENSIONLESS, "us/s", "microseconds/second", 0.0,
      1.0E-6),

  VOLUME_PERCENT("volume percent", "volpercent", Domain.DIMENSIONLESS, "volpercent", "volume percent", 0.0, 0.01),

  VOLUME_PARTS_PER_MILLION("volume parts per million", "volppm", Domain.DIMENSIONLESS, "volppm",
      "volume parts per million", 0.0, 1.0E-6),

  WEEKS("weeks", "wk", Domain.TIME, "wk", "weeks", 0.0, 604800.0),

  WEIGHT_PERCENT("weight percent", "wtpercent", Domain.DIMENSIONLESS, "wtpercent", "weight percent", 0.0, 0.01),

  WEIGHT_PARTS_PER_MILLION("weight parts per million", "wtppm", Domain.DIMENSIONLESS, "wtppm",
      "weight parts per million", 0.0, 1.0E-6),

  YARDS("yards", "yd", Domain.DISTANCE, "yd", "yards", 0.0, 0.9144),

  YARDS_PER_FOOT("yards/foot", "yd/ft", Domain.DIMENSIONLESS, "yd/ft", "yards/foot", 0.0, 3.0),

  TENTH_YARD("tenth yard", "yd(0.1)", Domain.DISTANCE, "yd(0.1)", "one tenth of a yard", 0.0, 0.09144),

  BENOITS_YARD_1895_A("Benoits yard (1895 A)", "ydBnA", Domain.DISTANCE, "ydBnA", "Benoits yard (1895 A)", 0.0,
      0.9143992),

  BENOITS_YARD_1895_B("Benoits yard (1895 B)", "ydBnB", Domain.DISTANCE, "ydBnB", "Benoits yard (1895 B)", 0.0,
      0.914399204289812),

  CLARKES_YARD("Clarkes yard", "ydCla", Domain.DISTANCE, "ydCla", "Clarkes yard", 0.0, 0.914391795),

  IMPERIAL_YARD("imperial yard", "ydIm", Domain.DISTANCE, "ydIm", "imperial yard", 0.0, 0.914391795),

  INDIAN_YARD("Indian yard", "ydInd", Domain.DISTANCE, "ydInd", "Indian yard", 0.0, 0.914398530744441),

  INDIAN_YARD_1937("Indian yard (1937)", "ydInd(37)", Domain.DISTANCE, "ydInd(37)", "Indian yard (1937)", 0.0,
      0.91439523),

  INDIAN_YARD_1962("Indian yard (1962)", "ydInd(62)", Domain.DISTANCE, "ydInd(62)", "Indian yard (1962)", 0.0,
      0.9143988),

  INDIAN_YARD_1975("Indian yard (1975)", "ydInd(75)", Domain.DISTANCE, "ydInd(75)", "Indian yard (1975)", 0.0,
      0.9143985),

  SEARS_YARD("Sears yard", "ydSe", Domain.DISTANCE, "ydSe", "Sears yard", 0.0, 0.914398414616029),

  YEARS("years", "yr", Domain.TIME, "yr", "years", 0.0, 3.155815E7);

  private final String _name;

  private final String _symbol;

  private final Domain _domain;

  private final String _standardForm;

  private final String _description;

  private final double _offset;

  private final double _scale;

  private final boolean _isCommon;

  /**
   * Constructs a unit of measurement.
   * @param name the unit name.
   * @param symbol the unit symbol.
   * @param domain the unit domain.
   * @param standardForm the standard form of the unit.
   * @param description the unit description.
   * @param offset the offset used in conversions.
   * @param scale the scalar used in conversions.
   */
  private Unit(final String name, final String symbol, final Domain domain, final String standardForm, final String description, final double offset, final double scale, final boolean isCommon) {
    _name = name;
    _symbol = symbol;
    _domain = domain;
    _standardForm = standardForm;
    _description = description;
    _offset = offset;
    _scale = scale;
    _isCommon = isCommon;
  }

  /**
   * Constructs a unit of measurement.
   * @param name the unit name.
   * @param symbol the unit symbol.
   * @param domain the unit domain.
   * @param standardForm the standard form of the unit.
   * @param description the unit description.
   * @param offset the offset used in conversions.
   * @param scale the scalar used in conversions.
   */
  private Unit(final String name, final String symbol, final Domain domain, final String standardForm, final String description, final double offset, final double scale) {
    this(name, symbol, domain, standardForm, description, offset, scale, false);
  }

  /**
   * Returns the name of the unit.
   * For example, "milliseconds", "foot", etc.
   */
  public String getName() {
    return _name;
  }

  /**
   * Returns the symbol of the unit.
   * For example, the symbol for MILLISECONDS is "ms".
   */
  public String getSymbol() {
    return _symbol;
  }

  /**
   * Returns the standard form of the unit.
   */
  public String standardForm() {
    return _standardForm;
  }

  /**
   * Returns the domain of the unit.
   * For example, the domain of the SECONDS unit is Domain.TIME.
   */
  public Domain getDomain() {
    return _domain;
  }

  /**
   * Returns a description of the unit.
   */
  public String getDescription() {
    return _description;
  }

  /**
   * Returns the offset value used in unit conversion.
   */
  public double getOffset() {
    return _offset;
  }

  /**
   * Returns the scale factor used in unit conversion.
   */
  public double getScale() {
    return _scale;
  }

  /**
   * Returns the string representation of the unit, which is simply the name.
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * Returns <i>true</i> if the unit is defined as a "common" one; <i>false</i> if not.
   * Common units are such things as FOOT, METER, SECOND, etc.
   * An example of an uncommon units is GOLD_COAST_FOOT.
   */
  public boolean getIsCommon() {
    return _isCommon;
  }

  public final static String DOMAIN = "domain";

  /** The sorted array of units. */
  private static String[] _sortedUnitNames;

  /** The sorted array of units. */
  private static String[] _sortedDomainNames;

  /**
   * Finds a unit by its symbol.
   * 
   * @param symbol of the unit 
   * @return the unit with the specified symbol 
   * @throws IllegalArgumentException if no unit found.
   */
  public static Unit lookupBySymbol(final String symbol) {

    Unit result = null;

    for (Unit unit : values()) {
      if (unit.getSymbol().equals(symbol)) {
        result = unit;
        break;
      }
    }

    if (result == null) {
      throw new IllegalArgumentException("Unit with symbol " + symbol + " not found");
    }

    return result;
  }

  /**
   * Finds a unit by its name.
   * 
   * @param name of the unit
   * @return the unit with the specified name
   * @throws IllegalArgumentException if no unit found.
   */
  public static Unit lookupByName(final String name) {

    //System.out.println("Unit.lookupByName: " + name);
    Unit result = null;

    for (Unit unit : values()) {
      if (unit.getName().equals(name)) {
        result = unit;
        break;
      }
    }

    if (result == null) {
      throw new IllegalArgumentException("Unit with name " + name + " not found");
    }

    return result;
  }

  /**
   * Return an array of units, filtered by a domain.
   * 
   * @param domain the domain to use for filtering.
   * @return the array of filtered units.
   */
  public static Unit[] getUnitsByDomain(final Domain domain) {
    return getUnitsByDomain(domain, false);
  }

  /**
   * Return an array of "common" units, filtered by a domain.
   * 
   * @param domain the domain to use for filtering.
   * @return the array of filtered units.
   */
  public static Unit[] getCommonUnitsByDomain(final Domain domain) {
    return getUnitsByDomain(domain, true);
  }

  /**
   * Return an array of units, filtered by a domain.
   * 
   * @param domain the domain to use for filtering.
   * @param findCommonOnly <i>true</i> to return only those units defined as "common".
   * @return the array of filtered units.
   */
  private static Unit[] getUnitsByDomain(final Domain domain, final boolean findCommonlyUsedOnly) {
    return getUnitListByDomain(domain, findCommonlyUsedOnly).toArray(new Unit[0]);
  }

  /**
   * Return an array of "common" units, filtered by an array of domains.
   * 
   * @param domains the array of domains to use for filtering.
   * @return the array of filtered units.
   */
  public static Unit[] getCommonUnitsByDomain(final Domain[] domains) {
    return getUnitListByDomains(domains, true).toArray(new Unit[0]);
  }

  /**
   * Return a list of unit names, filtered by a domain.
   * 
   * @param domain the domain to use for filtering.
   * @param findCommonOnly <i>true</i> to return only those units defined as "common".
   * @return the filtered list of unit names.
   */
  public static List<String> getUnitNamesByDomain(final Domain domain, final boolean findCommonOnly) {
    List<String> unitNames = new ArrayList<String>();
    for (Unit unit : values()) {
      if (domain == null || unit.getDomain().equals(domain)) {
        if (findCommonOnly) {
          if (unit.getIsCommon()) {
            unitNames.add(unit.getName());
          }
        } else {
          unitNames.add(unit.getName());
        }
      }
    }
    Collections.sort(unitNames, Sorting.ALPHANUMERIC_COMPARATOR);
    return unitNames;
  }

  /**
   * Return a list of units, filtered by a domain.
   * 
   * @param domain the domain to use for filtering.
   * @param findCommonOnly <i>true</i> to return only those units defined as "common".
   * @return the filtered list of units.
   */
  public static List<Unit> getUnitListByDomain(final Domain domain, final boolean findCommonOnly) {
    List<String> unitNames = getUnitNamesByDomain(domain, findCommonOnly);

    List<Unit> units = new ArrayList<Unit>();
    for (String unitName : unitNames) {
      units.add(Unit.lookupByName(unitName));
    }
    unitNames.clear();
    return units;
  }

  /**
   * Return a list of units, filtered by an array of domains.
   * 
   * @param domains the domains to use for filtering.
   * @param findCommonOnly <i>true</i> to return only those units defined as "common".
   * @return the filtered list of units.
   */
  public static List<Unit> getUnitListByDomains(final Domain[] domains, final boolean findCommonOnly) {
    List<Unit> unitsList = new ArrayList<Unit>();
    for (Domain domain : domains) {
      unitsList.addAll(getUnitListByDomain(domain, findCommonOnly));
    }
    Set<Unit> unitSet = new HashSet<Unit>(unitsList);
    ArrayList<Unit> uniqueUnits = new ArrayList<Unit>(unitSet);
    Collections.sort(uniqueUnits);
    return uniqueUnits;
  }

  /** 
   * creates the list of units (everytime) and includes the undefined units
   * @return
   */
  public static String[] getListOfAllNames() {

    List<String> nameList = new ArrayList<String>();
    for (Unit unit : values()) {
      nameList.add(unit.getName());
    }
    Collections.sort(nameList, Sorting.ALPHANUMERIC_COMPARATOR);
    return nameList.toArray(new String[0]);
  }

  /**
   * Returns a list of unit names, sorted alpha-numerically.
   */
  public static String[] getListOfNames() {
    if (_sortedUnitNames != null) {
      return _sortedUnitNames;
    }

    List<String> nameList = new ArrayList<String>();
    for (Unit unit : values()) {
      if (!unit.equals(Unit.UNDEFINED)) {
        nameList.add(unit.getName());
      }
    }
    Collections.sort(nameList, Sorting.ALPHANUMERIC_COMPARATOR);
    _sortedUnitNames = nameList.toArray(new String[0]);
    return _sortedUnitNames;
  }

  /**
   * Returns a list of unit domains, sorted alpha-numerically.
   */
  public static String[] getListOfDomainNames() {

    if (_sortedDomainNames != null) {
      return _sortedDomainNames;
    }

    List<String> definedDomains = new ArrayList<String>();
    for (Unit unit : values()) {
      if (!unit.equals(Unit.UNDEFINED)) {
        Domain domain = unit.getDomain();
        if (!definedDomains.contains(domain.getTitle())) {
          definedDomains.add(domain.getTitle());
        }
      }
    }

    Collections.sort(definedDomains, Sorting.ALPHANUMERIC_COMPARATOR);
    _sortedDomainNames = definedDomains.toArray(new String[0]);
    return _sortedDomainNames;
  }

  /**
   * Converts an input double value from one unit to another.
   * For example, this can be used to convert a value from milliseconds to seconds.
   * The input and output units must be of the same domain.
   * 
   * @param inputValue the input value.
   * @param inputUnit the unit for the input array.
   * @param outputUnit the desired output unit.
   * @param inputNullValue if this value is found in the input, set the output to the outputNullValue
   * @return the converted values.
   */
  public static double convert(final double inputValue, final Unit inputUnit, final Unit outputUnit,
      final double inputNullValue, final double outputNullValue) throws IllegalArgumentException {

    if (inputUnit == null || inputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("The input unit is null. Cannot convert.");
    }

    if (outputUnit == null || outputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("The output unit is null. Cannot convert.");
    }

    if (!inputUnit.getDomain().equals(outputUnit.getDomain())) {
      throw new IllegalArgumentException("Incompatible units: " + inputUnit.getName() + " and " + outputUnit.getName());
    }

    // For comparing ordinary values. 
    if (inputValue == inputNullValue) {
      return outputNullValue;
    }
    // for comparing infinites, NaNs, etc
    if (new Double(inputValue).equals(new Double(inputNullValue))) {
      return outputNullValue;
    }

    // If the 2 units are identical, no work needs to be done
    if (inputUnit.equals(outputUnit)) {
      return inputValue;
    }

    double outputValue = inputUnit.getOffset() + inputUnit.getScale() * inputValue;
    outputValue = (outputValue - outputUnit.getOffset()) / outputUnit.getScale();

    return outputValue;
  }

  /**
   * Converts an input double value from one unit to another.
   * For example, this can be used to convert a value from milliseconds to seconds.
   * The input and output units must be of the same domain.
   * 
   * @param inputValue the input value.
   * @param inputUnit the unit for the input array.
   * @param outputUnit the desired output unit.
   * @return the converted values.
   */
  public static double convert(final double inputValue, final Unit inputUnit, final Unit outputUnit) throws IllegalArgumentException {

    if (inputUnit == null || inputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("The input unit is null. Cannot convert.");
    }

    if (outputUnit == null || outputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("The output unit is null. Cannot convert.");
    }

    if (!inputUnit.getDomain().equals(outputUnit.getDomain())) {
      throw new IllegalArgumentException("Incompatible units: " + inputUnit.getName() + " and " + outputUnit.getName());
    }

    // If the 2 units are identical, no work needs to be done
    if (inputUnit.equals(outputUnit)) {
      return inputValue;
    }

    double outputValue = inputUnit.getOffset() + inputUnit.getScale() * inputValue;
    outputValue = (outputValue - outputUnit.getOffset()) / outputUnit.getScale();

    return outputValue;
  }

  /**
   * Converts an input float value from one unit to another.
   * For example, this can be used to convert a value from milliseconds to seconds.
   * The input and output units must be of the same domain.
   * 
   * @param inputValue the input value.
   * @param inputUnit the unit for the input array.
   * @param outputUnit the desired output unit.
   * @param inputNullValue the value for null test
   * @param outputNullValue value to return if input is null.
   * @return the converted values.
   */
  public static float convert(final float inputValue, final Unit inputUnit, final Unit outputUnit,
      final float inputNullValue, final float outputNullValue) throws IllegalArgumentException {

    if (inputUnit == null || inputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("The input unit is null. Cannot convert.");
    }

    if (outputUnit == null || outputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("The output unit is null. Cannot convert.");
    }

    if (!inputUnit.getDomain().equals(outputUnit.getDomain())) {
      throw new IllegalArgumentException("Incompatible units: " + inputUnit.getName() + " and " + outputUnit.getName());
    }

    // for testing ordinary values
    if (inputValue == inputNullValue) {
      return outputNullValue;
    }
    // for testing infinites, NaNs, etc
    if (new Float(inputValue).equals(new Float(inputNullValue))) {
      return outputNullValue;
    }

    // If the 2 units are identical, no work needs to be done
    if (inputUnit.equals(outputUnit)) {
      return inputValue;
    }

    double value = inputUnit.getOffset() + inputUnit.getScale() * inputValue;
    value = (value - outputUnit.getOffset()) / outputUnit.getScale();
    float outputValue = (float) value;

    return outputValue;
  }

  /**
   * Converts an input float value from one unit to another.
   * For example, this can be used to convert a value from milliseconds to seconds.
   * The input and output units must be of the same domain.
   * 
   * @param inputValue the input value.
   * @param inputUnit the unit for the input array.
   * @param outputUnit the desired output unit.
   * @return the converted values.
   */
  public static float convert(final float inputValue, final Unit inputUnit, final Unit outputUnit) throws IllegalArgumentException {

    if (inputUnit == null || inputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("The input unit is null. Cannot convert.");
    }

    if (outputUnit == null || outputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("The output unit is null. Cannot convert.");
    }

    if (!inputUnit.getDomain().equals(outputUnit.getDomain())) {
      throw new IllegalArgumentException("Incompatible units: " + inputUnit.getName() + " and " + outputUnit.getName());
    }

    // If the 2 units are identical, no work needs to be done
    if (inputUnit.equals(outputUnit)) {
      return inputValue;
    }

    double value = inputUnit.getOffset() + inputUnit.getScale() * inputValue;
    value = (value - outputUnit.getOffset()) / outputUnit.getScale();
    float outputValue = (float) value;

    return outputValue;
  }

  /**
   * Converts an array of input double values from one unit to another.
   * For example, this can be used to convert values from milliseconds to seconds.
   * The input and output units must be of the same domain.
   * 
   * @param inputValues the array of input values.
   * @param inputUnit the unit for the input array.
   * @param outputUnit the desired output unit.
   * @return the converted values.
   */
  public static double[] convert(final double[] inputValues, final Unit inputUnit, final Unit outputUnit) throws IllegalArgumentException {

    if (inputUnit == null || inputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("input unit is undefined. Cannot convert.");
    }

    if (outputUnit == null || outputUnit == Unit.UNDEFINED) {
      throw new IllegalArgumentException("output unit is undefined. Cannot convert.");
    }

    if (!inputUnit.getDomain().equals(outputUnit.getDomain())) {
      throw new IllegalArgumentException("Incompatible unit domains: " + inputUnit.getName() + " and "
          + outputUnit.getName());
    }

    double[] outputValues = new double[inputValues.length];

    // If the 2 units are identical, no work needs to be done
    if (inputUnit.equals(outputUnit)) {
      System.arraycopy(inputValues, 0, outputValues, 0, inputValues.length);
      return outputValues;
    }

    for (int i = 0; i < inputValues.length; i++) {
      double outputValue = inputUnit.getOffset() + inputUnit.getScale() * inputValues[i];
      outputValues[i] = (outputValue - outputUnit.getOffset()) / outputUnit.getScale();
    }
    return outputValues;
  }

}
