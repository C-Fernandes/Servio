export interface Availability {
    id?: number;
    dayOfWeek?: string;
    specificDate?: string;
    startTime: string;
    endTime: string;
    isAvailable?: boolean;
}

export interface Calendar {
    weeklyRules: Availability[];
    extraSlots: Availability[];
}

export interface TimeSlot {
    start: string;
    end: string;
}

export interface DaySchedule {
    name: string;
    label: string;
    slots: TimeSlot[];
}

export interface ExtraSlot {
    id?: number;
    date: string;
    startTime: string;
    endTime: string;
}
