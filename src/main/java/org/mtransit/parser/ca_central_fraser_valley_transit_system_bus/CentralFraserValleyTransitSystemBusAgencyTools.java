package org.mtransit.parser.ca_central_fraser_valley_transit_system_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// https://www.bctransit.com/open-data
// https://www.bctransit.com/data/gtfs/central-fraser-valley.zip
public class CentralFraserValleyTransitSystemBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-central-fraser-valley-transit-system-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new CentralFraserValleyTransitSystemBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating CFV Transit System bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating CFV Transit System bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	private static final String INCLUDE_AGENCY_ID = "6"; // CFV Transit System only

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		//noinspection deprecation
		if (!INCLUDE_AGENCY_ID.equals(gRoute.getAgencyId())) {
			return true;
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
	}

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongNameOrDefault();
		routeLongName = CleanUtils.cleanSlashes(routeLongName);
		routeLongName = CleanUtils.cleanNumbers(routeLongName);
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String AGENCY_COLOR_GREEN = "34B233";// GREEN (from PDF Corporate Graphic Standards)
	// private static final String AGENCY_COLOR_BLUE = "002C77"; // BLUE (from PDF Corporate Graphic Standards)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			int rsn = Integer.parseInt(gRoute.getRouteShortName());
			switch (rsn) {
			// @formatter:off
			case 1: return "8CC63F";
			case 2: return "8077B6";
			case 3: return "F8931E";
			case 4: return "AC5C3B";
			case 5: return "A54499";
			case 6: return "00AEEF";
			case 7: return "00AA4F";
			case 9: return "A2BCCF";
			case 12: return "0073AE";
			case 15: return "49176D";
			case 16: return "B3AA7E";
			case 17: return "77AE99";
			case 21: return "7C3F25";
			case 22: return "FFC20E";
			case 23: return "A3BADC";
			case 24: return "ED1D8F";
			case 26: return "F49AC1";
			case 31: return "BF83B9";
			case 32: return "EC1D8D";
			case 33: return "367D0F";
			case 34: return "FFC10E";
			case 35: return "F78B1F";
			case 39: return "0073AD";
			case 40: return "49176D";
			case 66: return "0D4D8B";
			// @formatter:on
			default:
				throw new MTLog.Fatal("Unexpected route color for %s!", gRoute.toStringPlus());
			}
		}
		return super.getRouteColor(gRoute);
	}

	private static final String EXCHANGE_SHORT = "Exch";

	private static final String BOURQUIN_EXCHANGE = "Bourquin " + EXCHANGE_SHORT;
	private static final String CLEARBROOK = "Clearbrook";
	private static final String DOWNTOWN = "Downtown";
	private static final String HUNTINGDON = "Huntingdon";
	private static final String MC_MILLAN = "McMillan";
	private static final String SADDLE = "Saddle";
	private static final String SANDY_HILL = "Sandy Hl";
	private static final String SUMAS_CTR = "Sumas Ctr";
	private static final String SUMAS_MTN = "Sumas Mtn";
	private static final String UFV = "UFV";

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		//noinspection deprecation
		map2.put(24L, new RouteTripSpec(24L, // BECAUSE same head-sign for 2 differents trip direction
				0, MTrip.HEADSIGN_TYPE_STRING, "CW", // PM
				1, MTrip.HEADSIGN_TYPE_STRING, "CCW") // AM
				.addTripSort(0, //
						Arrays.asList( //
								"107500", // Bourquin Exchange Bay B
								"107021", // ++
								"107013", // ++
								"107166", // ++
								"107500" // Bourquin Exchange Bay B
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"107500", // Bourquin Exchange Bay B
								"107122", // ++
								"107303", // ++
								"107085", // ++
								"107500" // Bourquin Exchange Bay B
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(26L, new RouteTripSpec(26L, // BECAUSE same head-sign for 2 differents trip direction
				0, MTrip.HEADSIGN_TYPE_STRING, SANDY_HILL, //
				1, MTrip.HEADSIGN_TYPE_STRING, BOURQUIN_EXCHANGE) //
				.addTripSort(0, //
						Arrays.asList( //
								"108262", // Bourquin Exchange Bay D
								"107039", // ==
								"120016", // !=
								"105727", // != Eastbound 34970 block Old Clayburn
								"120017", // != Eastbound Sandy Hill at Old Clayburn => Bourquin Ex
								"107040", // !=
								"107048", // != Eastbound McKinley Drive at McKinley Place
								"107049", // != Northbound McKinley at Sandy Hill
								"107053" // != Southbound Old Clayburn at Sandy Hill => Bourquin Ex
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"120017", // != Eastbound Sandy Hill at Old Clayburn <= START
								"107390", // Southbound McKee at Selkirk
								"107067", // !=
								"107053", // != Southbound Old Clayburn at Sandy Hill <= START
								"107054", // != Southbound Old Clayburn at Burnside
								"120015", // !=
								"107068", // ==
								"107085", // ==
								"108262", // !+ Bourquin Exchange Bay D => Sandy Hl
								"107499" // != Bourquin Exchange Bay A
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(34L, new RouteTripSpec(34L, // BECAUSE same head-sign for 2 differents trip direction
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.NORTH.getId(), //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.SOUTH.getId()) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList( //
								"107784", // == != Downtown Exchange Bay B
								"107756", // != <>
								"107819", // == !=
								"107834" // Southbound Stave Lake at Dewdney Trunk
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList( //
								"107834", // Southbound Stave Lake at Dewdney Trunk
								"107847", // == !=
								"107756", // != <>
								"107784" // == != Downtown Exchange Bay B
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(35L, new RouteTripSpec(35L, // BECAUSE same head-sign for 2 different trip direction
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.EAST.getId(), //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_DIRECTION, MDirectionType.WEST.getId()) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList( //
								"107784", // Downtown Exchange Bay B
								"107855" // Northbound Draper at Douglas
						)) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList( //
								"107855", // Northbound Draper at Douglas
								"107784" // Downtown Exchange Bay B
						)) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, @NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2, @NotNull MTripStop ts1, @NotNull MTripStop ts2, @NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 1L) {
			if (Arrays.asList( //
					BOURQUIN_EXCHANGE, //
					UFV //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UFV, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 2L) {
			if (Arrays.asList( //
					BOURQUIN_EXCHANGE, // <>
					MC_MILLAN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(MC_MILLAN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 3L) {
			if (Arrays.asList( //
					SUMAS_CTR, // <>
					CLEARBROOK //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(CLEARBROOK, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					SUMAS_CTR, // <>
					HUNTINGDON //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HUNTINGDON, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4L) {
			if (Arrays.asList( //
					"CCW", //
					BOURQUIN_EXCHANGE, //
					DOWNTOWN, //
					SADDLE //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SADDLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 7L) {
			if (Arrays.asList( //
					BOURQUIN_EXCHANGE, //
					DOWNTOWN, //
					SUMAS_MTN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(SUMAS_MTN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 9L) {
			if (Arrays.asList( //
					BOURQUIN_EXCHANGE, //
					DOWNTOWN //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 31L) {
			if (Arrays.asList( //
					"Matsqui Vlg", //
					"Bourquin Exch" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Bourquin Exch", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33L) {
			if (Arrays.asList( //
					"Counterclockwise", //
					"Cedar Vly" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Cedar Vly", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 40L) {
			if (Arrays.asList( //
					"East Mission Night Route", //
					"East Side Night Route" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("East Side Night Route", mTrip.getHeadsignId());
				return true;
			}
		}
		throw new MTLog.Fatal("%s: Unexpected trips to merge: %s & %s!", mTrip.getRouteId(), mTrip, mTripToMerge);
	}

	private static final Pattern EXCHANGE = Pattern.compile("((^|\\W)(exchange)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "$2" + EXCHANGE_SHORT + "$4";

	private static final Pattern ENDS_WITH_CONNECTOR = Pattern.compile("( connector$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern STARTS_WITH_DASH = Pattern.compile("(^.*( )?- )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = EXCHANGE.matcher(tripHeadsign).replaceAll(EXCHANGE_REPLACEMENT);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = STARTS_WITH_DASH.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = ENDS_WITH_CONNECTOR.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = EXCHANGE.matcher(gStopName).replaceAll(EXCHANGE_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
