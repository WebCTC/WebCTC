const setupViewBoxEventsSP = (svg) => {
  const {fromEvent} = rxjs;
  const {map, switchMap, takeUntil} = rxjs.operators;

// setup pan

  const touchstart$ = fromEvent(svg, 'touchstart');
  const touchmove$ = fromEvent(svg, 'touchmove');
  const touchend$ = fromEvent(window, 'touchend');

  const move$ = touchstart$.pipe(
    switchMap(ts => {
      ts.preventDefault();
      if (ts.touches.length === 1) {
        let prevX = ts.touches[0].clientX;
        let prevY = ts.touches[0].clientY;
        return touchmove$.pipe(
          map(tm => {
            tm.preventDefault();

            const delta = {
              x: (tm.touches[0].clientX - prevX) * globalScale,
              y: (tm.touches[0].clientY - prevY) * globalScale
            };
            prevX = tm.touches[0].clientX;
            prevY = tm.touches[0].clientY;

            return delta;
          }),
          takeUntil(touchend$)
        );
      }
    })
  );

  const zoom$ = touchstart$.pipe(
    switchMap(ts => {
      ts.preventDefault();
      if (ts.touches.length === 2) {
        let prevPosition = getEventPosition(ts)
        let prevArea = getTouchesArea(ts)
        return touchmove$.pipe(
          map(tm => {
            tm.preventDefault();
            prevPosition = getEventPosition(tm);
            let area = getTouchesArea(tm)

            const delta = {
              position: position,
              svg: svg,
              scale: Math.pow(scaleFactor, area / prevArea)
            };

            prevArea = area;

            return delta;
          }),
          takeUntil(touchend$)
        );
      }
    })
  );

  const updateViewBoxMin = (dx, dy) => {
    const viewBoxList = svg.getAttribute('viewBox').split(' ');
    viewBoxList[0] = '' + (parseInt(viewBoxList[0]) - dx);
    viewBoxList[1] = '' + (parseInt(viewBoxList[1]) - dy);
    const viewBox = viewBoxList.join(' ');
    svg.setAttribute('viewBox', viewBox);
  };

  move$.subscribe(({x, y}) => {
    updateViewBoxMin(x, y)
  });

  zoom$.subscribe(({point, svg, scale}) => {
    globalScale *= scale
    zoomAtPoint(point, svg, scale)
  });

  const getEventPosition = (ev) => {
    let x, y;
    let prevX0 = ev.touches[0].clientX;
    let prevY0 = ev.touches[0].clientY;
    let prevX1 = ev.touches[1].clientX;
    let prevY1 = ev.touches[1].clientY;
    x = (prevX0 + prevX1) / 2
    y = (prevY0 + prevY1) / 2
    return {x, y};
  };

  const getTouchesArea = (ev) => {
    let x = ev.touches[0].clientX - ev.touches[1].clientX;
    let y = ev.touches[0].clientY - ev.touches[1].clientY;
    return x * y;
  };

  const scaleFactor = 1.2;

  const zoomAtPoint = (point, svg, scale) => {
    // normalized position from 0 to 1
    const sx = point.x / svg.clientWidth;
    const sy = point.y / svg.clientHeight;

    // get current viewBox
    const [minX, minY, width, height] = svg.getAttribute('viewBox')
      .split(' ')
      .map(s => parseFloat(s));

    const x = minX + width * sx;
    const y = minY + height * sy;

    const scaledWidth = width * scale;
    const scaledHeight = height * scale;
    const scaledMinX = x + scale * (minX - x);
    const scaledMinY = y + scale * (minY - y);

    const scaledViewBox = [scaledMinX, scaledMinY, scaledWidth, scaledHeight]
      .map(s => s.toFixed(2))
      .join(' ');

    svg.setAttribute('viewBox', scaledViewBox);
  };
}
