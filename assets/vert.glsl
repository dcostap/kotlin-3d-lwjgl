#version 400 core

in vec3 position;
in vec2 texture_coords;

//out vec2 out_texture_coords;
out vec3 out_color;

uniform mat4 transform_mat;

void main(void) {
    gl_Position = transform_mat * vec4(position, 1.0);
//    out_texture_coords = texture_coords;
    out_color = vec3(position.x + 0.5, position.y + 0.5, position.z);
}