import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AvailabilityService } from '../../services/availability/availability.service';
import { Calendar, DaySchedule, ExtraSlot } from '../../models/Availability';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.scss',
})
export class CalendarComponent implements OnInit {
  private availabilityService = inject(AvailabilityService);

  // Estrutura base da UI para a grade semanal
  weeklySchedule: DaySchedule[] = [
    { name: 'SUNDAY', label: 'Domingo', slots: [] },
    { name: 'MONDAY', label: 'Segunda', slots: [] },
    { name: 'TUESDAY', label: 'Terça', slots: [] },
    { name: 'WEDNESDAY', label: 'Quarta', slots: [] },
    { name: 'THURSDAY', label: 'Quinta', slots: [] },
    { name: 'FRIDAY', label: 'Sexta', slots: [] },
    { name: 'SATURDAY', label: 'Sábado', slots: [] },
  ];


  extraSlots: ExtraSlot[] = [];

  ngOnInit(): void {
    this.loadCalendar();
  }


  loadCalendar(): void {
    this.availabilityService.getCalendar().subscribe({
      next: (data: Calendar) => {
        this.weeklySchedule.forEach(day => day.slots = []);

        data.weeklyRules.forEach(rule => {
          const day = this.weeklySchedule.find(d => d.name === rule.dayOfWeek);
          if (day) {
            day.slots.push({
              start: this.formatTime(rule.startTime),
              end: this.formatTime(rule.endTime)
            });
          }
        });

        this.extraSlots = data.extraSlots.map(slot => ({
          id: slot.id,
          date: slot.specificDate || '',
          startTime: this.formatTime(slot.startTime),
          endTime: this.formatTime(slot.endTime)
        }));
      },
      error: (err) => console.error('Erro ao carregar agenda', err)
    });
  }

  /**
   * Salva todo o estado da tela no Backend (Sync)
   */
  saveChanges(): void {
    const request: Calendar = {
      weeklyRules: [],
      extraSlots: []
    };

    this.weeklySchedule.forEach(day => {
      day.slots.forEach(slot => {
        request.weeklyRules.push({
          dayOfWeek: day.name,
          startTime: slot.start,
          endTime: slot.end
        });
      });
    });

    this.extraSlots.forEach(extra => {
      request.extraSlots.push({
        specificDate: extra.date,
        startTime: extra.startTime,
        endTime: extra.endTime
      });
    });

    this.availabilityService.syncCalendar(request).subscribe({
      next: () => {
        alert('Agenda atualizada com sucesso!');
        this.loadCalendar();
      },
      error: (err) => console.error('Erro ao salvar', err)
    });
  }

  addSlot(day: DaySchedule): void {
    day.slots.push({ start: '08:00', end: '12:00' });
  }

  removeSlot(day: DaySchedule, index: number): void {
    day.slots.splice(index, 1);
  }

  openExtraSlotModal(): void {
    const mockDate = new Date().toISOString().split('T')[0];
    this.extraSlots.push({
      date: mockDate,
      startTime: '09:00',
      endTime: '10:00'
    });
  }

  deleteExtraSlot(index: number): void {
    this.extraSlots.splice(index, 1);
  }


  private formatTime(time: string): string {
    return time ? time.substring(0, 5) : '';
  }
}