package org.mtransit.parser.ca_central_fraser_valley_transit_system_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
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

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("%s: Unexpected trips to merge: %s & %s!", mTrip.getRouteId(), mTrip, mTripToMerge);
	}

	private static final Pattern BAY_AZ_ = CleanUtils.cleanWords("bay [a-z]");

	@NotNull
	@Override
	public String cleanDirectionHeadsign(boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = PARSE_HEAD_SIGN_WITH_DASH_.matcher(directionHeadSign).replaceAll(PARSE_HEAD_SIGN_WITH_DASH_KEEP_TO);
		directionHeadSign = CleanUtils.keepToAndRemoveVia(directionHeadSign);
		directionHeadSign = cleanHeadSign(directionHeadSign);
		directionHeadSign = BAY_AZ_.matcher(directionHeadSign).replaceAll(EMPTY);
		return directionHeadSign;
	}

	private static final String EXCHANGE_SHORT = "Exch";
	private static final Pattern EXCHANGE = Pattern.compile("((^|\\W)(exchange)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String EXCHANGE_REPLACEMENT = "$2" + EXCHANGE_SHORT + "$4";

	private static final Pattern ENDS_WITH_CONNECTOR = Pattern.compile("( connector$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern PARSE_HEAD_SIGN_WITH_DASH_ = Pattern.compile("(^" +
			"([^\\-]+ -)?" + // from?
			"([^\\-]+)" + // to
			"(- ([^\\-]+))?" + // via?
			")", Pattern.CASE_INSENSITIVE);
	private static final String PARSE_HEAD_SIGN_WITH_DASH_KEEP_TO = "$3"; // "from - to - via" <= keep to

	private static final Pattern PARSE_HEAD_SIGN_WITH_DASH_FROM_ = Pattern.compile("(^" +
			"([^\\-]+ -)" + // from
			")", Pattern.CASE_INSENSITIVE);

	private static final Pattern PARSE_HEAD_SIGN_WITH_DASH_TO_VIA_ = Pattern.compile("(^" +
			"([^\\-]+)" + // to
			"(- ([^\\-]+))" + // via
			")", Pattern.CASE_INSENSITIVE);
	private static final String PARSE_HEAD_SIGN_WITH_DASH_TO_VIA_REPLACEMENT = "$2via $4"; // "to - via"-> "to via via"

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = PARSE_HEAD_SIGN_WITH_DASH_FROM_.matcher(tripHeadsign).replaceAll(EMPTY); // remove "from - "
		tripHeadsign = PARSE_HEAD_SIGN_WITH_DASH_TO_VIA_.matcher(tripHeadsign).replaceAll(PARSE_HEAD_SIGN_WITH_DASH_TO_VIA_REPLACEMENT); // "to via via"
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		return cleanHeadSign(tripHeadsign);
	}

	@NotNull
	private String cleanHeadSign(@NotNull String tripHeadsign) {
		tripHeadsign = EXCHANGE.matcher(tripHeadsign).replaceAll(EXCHANGE_REPLACEMENT);
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
