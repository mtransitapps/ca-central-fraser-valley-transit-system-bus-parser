package org.mtransit.parser.ca_central_fraser_valley_transit_system_bus;

import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// http://bctransit.com/*/footer/open-data
// http://bctransit.com/servlet/bctransit/data/GTFS.zip
public class CentralFraserValleyTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-central-fraser-valley-transit-system-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new CentralFraserValleyTransitSystemBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("Generating CFV Transit System bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating CFV Transit System bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String INCLUDE_AGENCY_ID = "6"; // CFV Transit System only

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (!INCLUDE_AGENCY_ID.equals(gRoute.agency_id)) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.route_short_name); // use route short name as route ID
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.route_long_name;
		routeLongName = MSpec.CLEAN_SLASHES.matcher(routeLongName).replaceAll(MSpec.CLEAN_SLASHES_REPLACEMENT);
		routeLongName = MSpec.cleanNumbers(routeLongName);
		routeLongName = MSpec.cleanStreetTypes(routeLongName);
		return MSpec.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_8CC63F = "8CC63F";
	private static final String COLOR_8077B6 = "8077B6";
	private static final String COLOR_F8931E = "F8931E";
	private static final String COLOR_AC5C3B = "AC5C3B";
	private static final String COLOR_A54499 = "A54499";
	private static final String COLOR_00AEEF = "00AEEF";
	private static final String COLOR_00AA4F = "00AA4F";
	private static final String COLOR_0073AE = "0073AE";
	private static final String COLOR_B3AA7E = "B3AA7E";
	private static final String COLOR_77AE99 = "77AE99";
	private static final String COLOR_7C3F25 = "7C3F25";
	private static final String COLOR_FFC20E = "FFC20E";
	private static final String COLOR_A3BADC = "A3BADC";
	private static final String COLOR_ED1D8F = "ED1D8F";
	private static final String COLOR_F49AC1 = "F49AC1";
	private static final String COLOR_BF83B9 = "BF83B9";
	private static final String COLOR_EC1D8D = "EC1D8D";
	private static final String COLOR_367D0F = "367D0F";
	private static final String COLOR_FFC10E = "FFC10E";
	private static final String COLOR_F78B1F = "F78B1F";
	private static final String COLOR_0073AD = "0073AD";
	private static final String COLOR_49176D = "49176D";
	private static final String COLOR_0D4D8B = "0D4D8B";

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.route_color)) {
			int rsn = Integer.parseInt(gRoute.route_short_name);
			switch (rsn) {
			// @formatter:off
			case 1: return COLOR_8CC63F;
			case 2: return COLOR_8077B6;
			case 3: return COLOR_F8931E;
			case 4: return COLOR_AC5C3B;
			case 5: return COLOR_A54499;
			case 6: return COLOR_00AEEF;
			case 7: return COLOR_00AA4F;
			case 12: return COLOR_0073AE;
			case 15: return COLOR_49176D;
			case 16: return COLOR_B3AA7E;
			case 17: return COLOR_77AE99;
			case 21: return COLOR_7C3F25;
			case 22: return COLOR_FFC20E;
			case 23: return COLOR_A3BADC;
			case 24: return COLOR_ED1D8F;
			case 26: return COLOR_F49AC1;
			case 31: return COLOR_BF83B9;
			case 32: return COLOR_EC1D8D;
			case 33: return COLOR_367D0F;
			case 34: return COLOR_FFC10E;
			case 35: return COLOR_F78B1F;
			case 39: return COLOR_0073AD;
			case 40: return COLOR_49176D;
			case 66: return COLOR_0D4D8B;
			// @formatter:on
			default:
				return AGENCY_COLOR_BLUE;
			}
		}
		return super.getRouteColor(gRoute);
	}

	private static final String AUGUSTON = "Auguston";
	private static final String CLOCK_WISE = "ClockWise";
	private static final String MISSION = "Mission";
	private static final String ABBOTSFORD = "Abbotsford";
	private static final String DOWNTOWN_EX = "Downtown Ex";
	private static final String HATZIC = "Hatzic";
	private static final String COUNTER_CLOCK_WISE = "CounterClockWise";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.id == 2l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.id == 3l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.id == 4l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(COUNTER_CLOCK_WISE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 5l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.id == 6l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(COUNTER_CLOCK_WISE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 7l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(COUNTER_CLOCK_WISE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 12l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 15l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DOWNTOWN_EX, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(AUGUSTON, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 16l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.id == 17l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(CLOCK_WISE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 21l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.id == 31l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(MISSION, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(ABBOTSFORD, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 32l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(COUNTER_CLOCK_WISE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 33l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(COUNTER_CLOCK_WISE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 34l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.id == 35l) {
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(DOWNTOWN_EX, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(HATZIC, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == 40l) {
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(COUNTER_CLOCK_WISE, gTrip.direction_id);
				return;
			}
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.trip_headsign), gTrip.direction_id);
	}

	private static final Pattern EXCHANGE = Pattern.compile("(exchange)", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "Ex";

	private static final Pattern STARTS_WITH_NUMBER = Pattern.compile("(^[\\d]+[\\S]*)", Pattern.CASE_INSENSITIVE);

	private static final Pattern ENDS_WITH_VIA = Pattern.compile("( via .*$)", Pattern.CASE_INSENSITIVE);
	private static final Pattern STARTS_WITH_TO = Pattern.compile("(^.* to )", Pattern.CASE_INSENSITIVE);

	private static final Pattern AND = Pattern.compile("( and )", Pattern.CASE_INSENSITIVE);
	private static final String AND_REPLACEMENT = " & ";

	private static final Pattern CLEAN_P1 = Pattern.compile("[\\s]*\\([\\s]*");
	private static final String CLEAN_P1_REPLACEMENT = " (";
	private static final Pattern CLEAN_P2 = Pattern.compile("[\\s]*\\)[\\s]*");
	private static final String CLEAN_P2_REPLACEMENT = ") ";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = EXCHANGE.matcher(tripHeadsign).replaceAll(EXCHANGE_REPLACEMENT);
		tripHeadsign = ENDS_WITH_VIA.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = STARTS_WITH_TO.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = AND.matcher(tripHeadsign).replaceAll(AND_REPLACEMENT);
		tripHeadsign = CLEAN_P1.matcher(tripHeadsign).replaceAll(CLEAN_P1_REPLACEMENT);
		tripHeadsign = CLEAN_P2.matcher(tripHeadsign).replaceAll(CLEAN_P2_REPLACEMENT);
		tripHeadsign = STARTS_WITH_NUMBER.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = MSpec.cleanStreetTypes(tripHeadsign);
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static final Pattern STARTS_WITH_BOUND = Pattern.compile("(^(east|west|north|south)bound)", Pattern.CASE_INSENSITIVE);

	private static final Pattern AT = Pattern.compile("( at )", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = " / ";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = STARTS_WITH_BOUND.matcher(gStopName).replaceAll(StringUtils.EMPTY);
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = MSpec.cleanStreetTypes(gStopName);
		return MSpec.cleanLabel(gStopName);
	}
}
