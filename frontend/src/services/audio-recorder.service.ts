import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AudioRecorderService implements OnDestroy {
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: Blob[] = [];
  private audioBlobSubject = new Subject<Blob>();
  private recordingStateSubject = new Subject<boolean>();
  private stream: MediaStream | null = null;

  isRecording$ = this.recordingStateSubject.asObservable();

  async startRecording(): Promise<void> {
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      return;
    }
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.mediaRecorder = new MediaRecorder(this.stream);
      this.mediaRecorder.ondataavailable = event => this.audioChunks.push(event.data);
      this.mediaRecorder.onstop = () => {
        const audioBlob = new Blob(this.audioChunks, { type: 'audio/wav' });
        this.audioBlobSubject.next(audioBlob);
        this.audioChunks = [];
        this.recordingStateSubject.next(false);
        this.stopStream();
      };
      this.mediaRecorder.start();
      this.recordingStateSubject.next(true);
    } catch (err) {
      console.error('Error starting recording:', err);
      this.recordingStateSubject.next(false);
      // Handle errors (e.g., permission denied)
    }
  }

  stopRecording(): void {
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.stop();
    }
  }

  getRecordedBlob(): Observable<Blob> {
    return this.audioBlobSubject.asObservable();
  }

  private stopStream(): void {
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }
  }

  ngOnDestroy(): void {
    this.stopStream();
  }
}
