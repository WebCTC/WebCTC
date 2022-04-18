const RAIL_DATA_URL = '/api/rails/'
const SIGNAL_DATA_URL = '/api/signals/'
const FORMATION_DATA_URL = '/api/formations/'
const TRAIN_DATA_URL = '/api/trains/'

let globalScale = 1.0;
let minX = 0;
let minZ = 0;
let maxX = 0;
let maxZ = 0;
let scale = 1;

async function updateRail(svg) {
  return Promise.resolve()
    .then(await fetch(RAIL_DATA_URL)
      .then(res => res.json())
      .then(json => {
        let updateList = Array.from(document.querySelectorAll("[id^='rail']"))

        json.forEach(railCore => {
          let pos = railCore["pos"];
          let id = "rail," + pos[0] + "," + pos[1] + "," + pos[2] + ","
          let isTrainOnRail = railCore["isTrainOnRail"]
          let isStraightRail = railCore["railMaps"].every(railMap => railMap["isNotActive"] === undefined)

          let group = document.getElementById(id)
          if (group != null && isStraightRail) {
            updateList = updateList.filter(n => n !== group)
            group.setAttribute('stroke', isTrainOnRail ? 'red' : 'white')
          } else {
            group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
            group.id = id
            group.setAttribute('stroke-width', '1.5px');
            railCore["railMaps"].sort(railMap => railMap["isNotActive"] === true ? -1 : 1).forEach(railMap => {
              let startRP = railMap["startRP"];
              let endRP = railMap["endRP"];
              if (startRP != null && endRP != null) {
                let isNotActive = railMap["isNotActive"] === true;
                let startPosX = startRP["posX"]
                let startPosZ = startRP["posZ"]
                let endPosX = endRP["posX"]
                let endPosZ = endRP["posZ"]
                let line = createLine(startPosX, startPosZ, endPosX, endPosZ)
                if (!isStraightRail) {
                  line.setAttribute('stroke', isTrainOnRail ? 'red' : isNotActive ? 'gray' : 'white')
                }
                group.appendChild(line)
              }
            });
            svg.appendChild(group)
          }
        });

        updateList.forEach(n => n.remove());
      }))
    .then(await fetch(SIGNAL_DATA_URL)
      .then(res => res.json())
      .then(json => {
        let updateList = Array.from(document.querySelectorAll("[id^='signal']"))

        json.forEach(signal => {
          let pos = signal["pos"]
          let rotation = signal["rotation"]
          let blockDirection = signal["blockDirection"]
          let cos = Math.cos((270 + rotation) * (Math.PI / 180))
          let sin = Math.sin((270 + rotation) * (Math.PI / 180))
          let signalLevel = signal["signalLevel"]
          let p = (blockDirection * 90 - rotation + 360) % 360
          let fixX = (45 < p && p < 135) ? 3 : ((225 < p && p < 315) ? -3 : 0);

          let posX = pos[0]
          let posZ = pos[2]
          let id = "signal," + pos[0] + "," + pos[2] + ","

          let circle = this.createSignalCircle(posX, pos[1], posZ, signalLevel, fixX)

          let group = document.getElementById(id)
          if (group == null) {
            group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
            group.id = id

            let line = createLineFromComponent(
              posX + 1.5 * cos, posZ - 1.5 * sin,
              2.5 * cos, -2.5 * sin
            )
            line.setAttribute('stroke', 'lightgray');
            line.setAttribute('stroke-width', '0.5px');
            line.setAttribute('name', 'verticalLine');
            group.appendChild(line);

            let baseLine = createLineFromComponent(
              posX + 4 * cos + 1.5 * sin, posZ - 4 * sin + 1.5 * cos,
              -3.0 * sin, -3.0 * cos
            )
            baseLine.setAttribute('stroke', 'lightgray');
            baseLine.setAttribute('stroke-width', '0.5px');
            baseLine.setAttribute('name', 'horizontalLine');
            group.appendChild(baseLine);

            group.appendChild(circle)

            svg.appendChild(group);
          } else {
            updateList = updateList.filter(n => n !== group)
            let minus = 0
            let last;
            let circleArray = Array.from(group.getElementsByTagName('circle'))
            let support
            if ((support = group.getElementsByTagName('g')[0]) == null) {
              support = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            }
            support.innerHTML = ""

            let circleCache = circleArray.find(c => Number(c.getAttribute("yCoord")) === Number(pos[1]))
            if (circleCache == null) {
              circleArray.push(circle)
            } else {
              circleCache.setAttribute('fill', getSignalColor(signalLevel))
            }

            if (circleArray.length > 1) {
              circleArray
                .sort((a, b) => Number(a.getAttribute("yCoord")) < Number(b.getAttribute("yCoord")) ? -1 : 1)
                .forEach((value, index) => {
                  let fixX = Number(value.getAttribute("fix"))
                  if (fixX === 0 && index === 0) {
                    minus += (index - last) ? -1.5 : +1.5
                    last = index
                    fixX = Number(circleArray[index + 1].getAttribute("fix")) * -1
                  } else if (fixX !== 0 && index !== 0) {
                    minus += (index - last) ? -1.5 : +1.5
                    last = index
                    fixX = 0;
                  } else if (fixX !== 0) {
                    minus += (index - last) ? -1.5 : +1.5
                    last = index
                  } else if (index - last) {
                    minus -= 1.0
                  }
                  let cx = posX - (3.5 * index + minus) * cos - fixX * sin
                  let cy = posZ + (3.5 * index + minus) * sin - fixX * cos
                  value.setAttribute('cx', cx)
                  value.setAttribute('cy', cy)
                  group.appendChild(value)

                  if (fixX !== 0 || fixX === 0 && index === 0) {
                    let polyline = document.createElementNS('http://www.w3.org/2000/svg', 'polyline');
                    polyline.setAttribute('points',
                      (cx + 1.5 * cos) + "," + (cy - 1.5 * sin) + " " +
                      (cx + 3 * cos) + "," + (cy - 3 * sin) + " " +
                      (cx + 3 * cos + fixX * sin) + "," + (cy - 3 * sin + fixX * cos)
                    );
                    polyline.setAttribute('stroke', 'lightgray');
                    polyline.setAttribute('stroke-width', '0.5px');
                    polyline.setAttribute('name', 'holizonalLine');
                    polyline.setAttribute('fill', 'none');
                    support.appendChild(polyline);
                  }
                })
            }
            Array.from(group.getElementsByTagName('line'))
              .filter(line => line.getAttribute("name") === "verticalLine")
              .find(line => {
                line.setAttribute('x1', posX + (1.5 - 2.5 * circleArray.length - minus) * cos);
                line.setAttribute('y1', posZ - (1.5 - 2.5 * circleArray.length - minus) * sin);
              })
            group.appendChild(support);
          }
        });

        updateList.forEach(n => n.remove());
      }))
    .then(await fetch(FORMATION_DATA_URL)
      .then(res => res.json())
      .then(json => {
        let updateList = Array.from(document.querySelectorAll("[id^='formation']"))

        json.forEach(formation => {
          if (formation != null && formation["controlCar"] != null) {
            let id = "formation," + formation["id"] + ","
            let group = document.getElementById(id)

            if (group == null) {
              group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
              group.id = id
              svg.appendChild(group)
            } else {
              updateList = updateList.filter(n => n !== group)
            }
            json = formation["controlCar"]
            group.innerHTML = ""
            let pos = json["pos"]
            let posX = pos[0]
            let posZ = pos[2]

            let rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
            rect.setAttribute('x', String(posX - 3))
            rect.setAttribute('y', String(posZ - 3))
            rect.setAttribute('width', "6px")
            rect.setAttribute('height', "6px")
            rect.setAttribute('fill', "YELLOW")
            rect.setAttribute('stroke', "GRAY")
            group.appendChild(rect)

            let text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            text.textContent = (json["name"] === "no_name" ? "" : json["name"] + " ") + json["driver"]
            text.setAttribute('x', String(posX + 3))
            text.setAttribute('y', String(posZ + 2))
            text.setAttribute('font-size', "8")
            text.setAttribute('font-weight', "bold")
            text.setAttribute('fill', "black")
            group.appendChild(text)
          }
        });

        updateList.forEach(n => n.remove());
      }))
}


function getSignalColor(signalLevel) {
  switch (signalLevel) {
    case 0:
      return "darkslategray"
    case 1:
      return "rgb(2045,0,0)"
    case 2:
      return "rgb(255,153,0)"
    case 3:
      return "rgb(255,204,0)"
    case 4:
      return "rgb(155,255,0)"
    case 5:
      return "rgb(51,204,0)"
    default:
      return "rgb(51,102,255)"
  }
}

function createSignalCircle(posX, yCoord, posZ, signalLevel, fixX) {
  let circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
  circle.setAttribute('yCoord', yCoord)
  circle.setAttribute('fix', fixX)
  circle.setAttribute('cx', posX)
  circle.setAttribute('cy', posZ)
  circle.setAttribute('r', "1.5px")
  circle.setAttribute('fill', getSignalColor(signalLevel))
  circle.setAttribute('stroke', "lightgray")
  circle.setAttribute('stroke-width', "0.5px")
  return circle
}

function createLineFromComponent(x, y, xComponent, yComponent) {
  return createLine(x, y, x + xComponent, y + yComponent)
}

function createLine(x1, y1, x2, y2) {
  let line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
  line.setAttribute('x1', String(x1));
  line.setAttribute('y1', String(y1));
  line.setAttribute('x2', String(x2));
  line.setAttribute('y2', String(y2));
  return line
}