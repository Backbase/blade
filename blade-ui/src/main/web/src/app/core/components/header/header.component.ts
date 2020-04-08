import { Component, OnInit } from '@angular/core';
import { fromEvent } from 'rxjs';
import { throttleTime, map, pairwise, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  isScrollingDown: boolean = false;

  constructor() {}

  ngOnInit() {}

  ngAfterViewInit() {
    const scroll$ = fromEvent(window, 'scroll')
      .pipe(
          map(() => window.pageYOffset)
      )

      scroll$.subscribe((res) => {
        this.isScrollingDown = (res >= 100) ? true : false
      });
  }
}
