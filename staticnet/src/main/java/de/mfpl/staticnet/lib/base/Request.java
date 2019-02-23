package de.mfpl.staticnet.lib.base;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.mfpl.staticnet.lib.data.LocationReference;
import de.mfpl.staticnet.lib.data.Trip;

public final class Request {

    @SerializedName("location")
    private LocationReference locationReference;

    @SerializedName("dateRange")
    private Filter.Date dateRange;

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("route_id")
    private String routeId;

    @SerializedName("block_id")
    private String blockId;

    @SerializedName("stop_id")
    private String stopId;

    @SerializedName("options")
    private Options options;

    @SerializedName("filter")
    private Filter filter;

    public LocationReference getLocationReference() {
        return locationReference;
    }

    public Request setLocationReference(LocationReference locationReference) {
        this.locationReference = locationReference;
        return this;
    }

    public Request setDateRange(Filter.Date dateRange) {
        this.dateRange = dateRange;
        return this;
    }

    public Request setTripId(String tripId) {
        this.tripId = tripId;
        return this;
    }

    public Request setRouteId(String routeId) {
        this.routeId = routeId;
        return this;
    }

    public Request setBlockId(String blockId) {
        this.blockId = blockId;
        return this;
    }

    public Request setStopId(String stopId) {
        this.stopId = stopId;
        return this;
    }

    public Request setOptions(Options options) {
        this.options = options;
        return this;
    }

    public Options getOptions() {
        return this.options;
    }

    public Request setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public final static class Options {

        @SerializedName("include_agency")
        private boolean includeAgency = false;

        @SerializedName("include_routes")
        private boolean includeRoutes = false;

        @SerializedName("include_stops")
        private boolean includeStops = false;

        @SerializedName("include_places")
        private boolean includePlaces = false;

        @SerializedName("include_shape")
        private boolean includeShape = false;

        @SerializedName("include_full_stop_times")
        private boolean includeFullStopTimes = false;

        @SerializedName("include_realtime")
        private boolean includeRealtime = false;

        @SerializedName("start_date")
        private Filter.Date startDate;

        @SerializedName("end_date")
        private Filter.Date endDate;

        @SerializedName("limit")
        private int limit = 10;

        public boolean isIncludeRoutes() {
            return includeRoutes;
        }

        public Options setIncludeRoutes(boolean includeRoutes) {
            this.includeRoutes = includeRoutes;
            return this;
        }

        public boolean isIncludeStops() {
            return includeStops;
        }

        public Options setIncludeStops(boolean includeStops) {
            this.includeStops = includeStops;
            return this;
        }

        public boolean isIncludePlaces() {
            return includePlaces;
        }

        public Options setIncludePlaces(boolean includePlaces) {
            this.includePlaces = includePlaces;
            return this;
        }

        public boolean isIncludeShape() {
            return includeShape;
        }

        public Options setIncludeShape(boolean includeShape) {
            this.includeShape = includeShape;
            return this;
        }

        public boolean isIncludeFullStopTimes() {
            return includeFullStopTimes;
        }

        public Options setIncludeFullStopTimes(boolean includeFullStopTimes) {
            this.includeFullStopTimes = includeFullStopTimes;
            return this;
        }

        public boolean isIncludeAgency() {
            return includeAgency;
        }

        public Options setIncludeAgency(boolean includeAgency) {
            this.includeAgency = includeAgency;
            return this;
        }

        public int getLimit() {
            return this.limit;
        }

        public Options setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public Filter.Date getStartDate() {return this.startDate;}

        public void setStartDate(Filter.Date startDate) {
            this.startDate = startDate;
        }

        public Filter.Date getEndDate() { return this.endDate;}

        public void setEndDate(Filter.Date endDate) {
            this.endDate = endDate;
        }

        public boolean isIncludeRealtime() {
            return includeRealtime;
        }

        public Options setIncludeRealtime(boolean includeRealtime) {
            this.includeRealtime = includeRealtime;
            return this;
        }

    }

    public final static class Filter {

        @SerializedName("date")
        private Date date;

        @SerializedName("time")
        private String time;

        @SerializedName("route_types")
        private int[] routeTypes;

        @SerializedName("wheelchair_accessible")
        private Trip.WheelchairAccessible wheelchairAccessible;

        @SerializedName("bikes_allowed")
        private Trip.BikesAllowed bikesAllowed;

        public Date getDate() {
            return date;
        }

        public Filter setDate(Date date) {
            this.date = date;
            return this;
        }

        public int[] getRouteTypes() {
            return routeTypes;
        }

        public Filter setRouteTypes(int[] routeTypes) {
            this.routeTypes = routeTypes;
            return this;
        }

        public String getTime() {
            return this.time;
        }

        public Filter setTime(String time) {
            this.time = time;
            return this;
        }

        public Trip.WheelchairAccessible getWheelchairAccessible() {
            return wheelchairAccessible;
        }

        public Filter setWheelchairAccessible(Trip.WheelchairAccessible wheelchairAccessible) {
            this.wheelchairAccessible = wheelchairAccessible;
            return this;
        }

        public Trip.BikesAllowed getBikesAllowed() {
            return bikesAllowed;
        }

        public Filter setBikesAllowed(Trip.BikesAllowed bikesAllowed) {
            this.bikesAllowed = bikesAllowed;
            return this;
        }

        public static final class Date {

            @SerializedName("single")
            private String single;

            @SerializedName("range")
            private Range range;

            public static Date fromJavaDate(java.util.Date javaDate) {
                return new Date(new SimpleDateFormat("yyyyMMdd").format(javaDate));
            }

            public static Date fromJavaDateRange(java.util.Date javaDateStart, java.util.Date javaDateEnd) {
                Date result = new Date(0);
                result.getRange().setStartDate(new SimpleDateFormat("yyyyMMdd").format(javaDateStart));
                result.getRange().setEndDate((new SimpleDateFormat("yyyyMMdd").format(javaDateEnd)));

                return result;
            }

            public java.util.Date toJavaDate() {
                try {
                    return new SimpleDateFormat("yyyyMMdd").parse(this.single);
                } catch (ParseException e) {
                    return null;
                }
            }

            private Date(String singleDate) {
                this.single = singleDate;
            }

            private Date(int signatureBuddy) {
                this.range = new Range();
            }

            public Date() {}

            public String getSingle() {
                return single;
            }

            public Date setSingle(String single) {
                this.single = single;
                return this;
            }

            public Range getRange() {
                return range;
            }

            public Date setRange(Range range) {
                this.range = range;
                return this;
            }

            public static final class Range {

                @SerializedName("start_date")
                private String startDate;

                @SerializedName("end_date")
                private String endDate;

                public String getStartDate() {
                    return startDate;
                }

                public Range setStartDate(String startDate) {
                    this.startDate = startDate;
                    return this;
                }

                public String getEndDate() {
                    return endDate;
                }

                public Range setEndDate(String endDate) {
                    this.endDate = endDate;
                    return this;
                }

            }
        }
    }
}
