
var margin = {top: 10, right: 30, bottom: 30, left: 60},
width = 460 - margin.left - margin.right,
height = 400 - margin.top - margin.bottom;

var svgCurved = d3.select("#curved")
  .append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform",
          "translate(" + margin.left + "," + margin.top + ")");

 d3.json("http://localhost:8230/settings/curved").then(function(d) {
  function translate(prevDuration, d){
    // nano to minutes
    var dur = d.duration / 60000000000
    var r = {
        duration : prevDuration + dur,
        shutterSpeed : d.shutterSpeed,
        iso : d.iso,
        ev : d.ev
    }
    console.log(r);
    return r;
  }

  function doIt(data) {
    // Add X axis --> it is a date format
    var x = d3.scaleTime()
      .domain(d3.extent(data, function(d) { return d.duration; }))
      .range([ 0, width ]);
    svgCurved.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x));

    // Add Y axis
    var ssy = d3.scaleLinear()
      .domain([0, d3.max(data, function(d) { return +d.shutterSpeed; })])
      .range([ height, 0 ]);
    svgCurved.append("g")
      .call(d3.axisLeft(ssy));

    var isoy = d3.scaleLinear()
      .domain([0, d3.max(data, function(d) { return +d.iso; })])
      .range([ height, 0 ]);
    svgCurved.append("g")
      .call(d3.axisLeft(isoy));

    var evy = d3.scaleLinear()
      .domain([0, d3.max(data, function(d) { return +d.ev; })])
      .range([ height, 0 ]);
    svgCurved.append("g")
      .call(d3.axisLeft(evy));

    svgCurved.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "steelblue")
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.duration) })
        .y(function(d) { return ssy(d.shutterSpeed) }))

    svgCurved.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "red")
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.duration) })
        .y(function(d) { return isoy(d.iso) }))

    svgCurved.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "black")
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.duration) })
        .y(function(d) { return evy(d.ev) }))
  }
  console.log(d)
  var td = []
  var prevDuration = 0
  for (i = 0; i < d.length; i++) {
    var n = d[i]
    td[i] = translate(prevDuration, n)
    prevDuration = td[i].duration
  }

  doIt(td)
})


var svgScaled = d3.select("#scaled")
  .append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform",
          "translate(" + margin.left + "," + margin.top + ")");

 d3.json("http://localhost:8230/settings/scaled").then(function(d) {
  function translate(d){
    var r = {
        time : d3.timeParse("%Y-%m-%dT%I:%M:%S")(d.time),
        shutterSpeed : d.shutterSpeed,
        iso : d.iso,
        ev : d.ev
    }
    console.log(r);
    return r;
  }

  function doIt(data) {
    // Add X axis --> it is a date format
    var x = d3.scaleTime()
      .domain(d3.extent(data, function(d) { return d.time; }))
      .range([ 0, width ]);
    svgScaled.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x));

    // Add Y axis
    var ssy = d3.scaleLinear()
      .domain([0, d3.max(data, function(d) { return +d.shutterSpeed; })])
      .range([ height, 0 ]);
    svgScaled.append("g")
      .call(d3.axisLeft(ssy));

    var isoy = d3.scaleLinear()
      .domain([0, d3.max(data, function(d) { return +d.iso; })])
      .range([ height, 0 ]);
    svgScaled.append("g")
      .call(d3.axisLeft(isoy));

    var evy = d3.scaleLinear()
      .domain([0, d3.max(data, function(d) { return +d.ev; })])
      .range([ height, 0 ]);
    svgScaled.append("g")
      .call(d3.axisLeft(evy));

    svgScaled.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "steelblue")
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.time) })
        .y(function(d) { return ssy(d.shutterSpeed) }))

    svgScaled.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "red")
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.time) })
        .y(function(d) { return isoy(d.iso) }))

    svgScaled.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "black")
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.time) })
        .y(function(d) { return evy(d.ev) }))
  }

  var td = []
  for (i = 0; i < d.length; i++) {
    var n = d[i]
    td[i] = translate(n)
  }
  console.log(td)

  doIt(td)
})