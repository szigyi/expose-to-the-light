
var margin = {top: 10, right: 30, bottom: 30, left: 60},
width = 460 - margin.left - margin.right,
height = 400 - margin.top - margin.bottom;

var svg = d3.select("#curvature")
  .append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
  .append("g")
    .attr("transform",
          "translate(" + margin.left + "," + margin.top + ")");

 d3.json("http://localhost:8230/curvature").then(function(d) {

  // When reading the csv, I must format variables:
  function translate(d){
    var r = { duration : d3.timeParse("%M")(d.duration), ev : d.shutterSpeed }
    console.log(r);
    return r;
  }

  // Now I can use this dataset:
  function doIt(data) {
    // Add X axis --> it is a date format
    var x = d3.scaleTime()
      .domain(d3.extent(data, function(d) { return d.duration; }))
      .range([ 0, width ]);
    svg.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x));

    // Add Y axis
    var ssy = d3.scaleLinear()
      .domain([0, d3.max(data, function(d) { return +d.shutterSpeed; })])
      .range([ height, 0 ]);
    svg.append("g")
      .call(d3.axisLeft(ssy));

    var isoy = d3.scaleLinear()
      .domain([0, d3.max(data, function(d) { return +d.iso; })])
      .range([ height, 0 ]);
    svg.append("g")
      .call(d3.axisLeft(isoy));


    // Add the line
    svg.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "steelblue")
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.duration) })
        .y(function(d) { return ssy(d.shutterSpeed) }))

    svg.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "red")
      .attr("stroke-width", 1.5)
      .attr("d", d3.line()
        .x(function(d) { return x(d.duration) })
        .y(function(d) { return isoy(d.iso) }))
  }
  console.log(d)
//  data.forEach(function(d) {
//        var dt = translate(d)
//  })

  doIt(d)
})