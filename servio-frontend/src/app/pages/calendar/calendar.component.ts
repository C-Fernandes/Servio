import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AvailabilityService } from '../../services/availability/availability.service';
import { Calendar, DaySchedule, ExtraSlot } from '../../models/Availability';
import { debounceTime, Subject } from 'rxjs';
import { ToastComponent } from '../../components/toast/toast.component';
import { ToastService } from '../../services/toast/toast.service';
import { AddExtraSlotModalComponent } from '../../components/add-extra-slot-modal/add-extra-slot-modal.component';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FormsModule, AddExtraSlotModalComponent],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.scss',
})
export class CalendarComponent implements OnInit {
  private availabilityService = inject(AvailabilityService);
  private autoSaveSubject = new Subject<void>();
  private toast = inject(ToastService); isModalOpen: boolean = false;
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
    this.autoSaveSubject.pipe(debounceTime(800)).subscribe(() => {
      this.executeSync();
    });
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
  private executeSync(): void {
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
        this.toast.showToast("Agenda atualizada com sucesso", "success")
      },
      error: (err) => {
        this.toast.showToast("Erro ao atualizar a agenda", "error");

        console.error('Erro ao salvar no auto-save', err);
      }
    });
  }

  triggerAutoSave(): void {
    this.autoSaveSubject.next();
  }

  addSlot(day: DaySchedule): void {
    day.slots.push({ start: '08:00', end: '12:00' });
  }

  removeSlot(day: DaySchedule, index: number): void {
    day.slots.splice(index, 1);
  }

  openExtraSlotModal(): void {
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  } deleteExtraSlot(index: number): void {
    this.extraSlots.splice(index, 1);
    this.triggerAutoSave();
  }

  handleNewExtraSlot(event: { startDate: string, startTime: string, endTime: string }): void {
    this.extraSlots.push({
      date: event.startDate,
      startTime: event.startTime,
      endTime: event.endTime
    });

    this.triggerAutoSave();
    this.closeModal();
  }


  private formatTime(time: string): string {
    return time ? time.substring(0, 5) : '';
  }
}