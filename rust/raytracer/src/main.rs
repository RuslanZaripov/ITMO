use image::{ImageBuffer, Rgb};

use crate::camera::Camera;
use crate::equation::Equation;
use crate::hittable::{Hittable, HittableList};
use crate::material::{Lambertian, Metal};
use crate::ray::Ray;
use crate::sphere::Sphere;
use crate::vec3::{Color, Vec3};
use crate::utils::get_random_double;

pub mod equation;
pub mod sphere;
pub mod hittable;
pub mod ray;
pub mod vec3;
pub mod material;
pub mod camera;
pub mod utils;

#[allow(unused_variables)]
fn main() {
    let ratio = 16.0 / 9.0;
    let width = 800;
    let height = (width as f64 / ratio) as u32;
    let samples_per_pixel = 100;
    let max_depth = 50;

    let mut scene: HittableList = HittableList::new();

    let material_ground: Lambertian = Lambertian::new(Color::new(0.8, 0.8, 0.0));
    let material_center: Lambertian = Lambertian::new(Color::new(0.7, 0.3, 0.3));
    let material_left: Metal   = Metal::new(Color::new(0.8, 0.8, 0.8));
    let material_right: Metal = Metal::new(Color::new(0.8, 0.6, 0.2));

    scene.add(Sphere::new(Vec3::new(0.0, -100.5, -1.0), 100.0, material_ground));
    scene.add(Sphere::new(Vec3::new(0.0, 0.0, -1.0), 0.5, material_center));
    scene.add(Sphere::new(Vec3::new(-1.0, 0.0, -1.0), 0.5, material_left));
    scene.add(Sphere::new(Vec3::new(1.0, 0.0, -1.0), 0.5, material_right));

    let camera = Camera::new();

    let mut img = image::ImageBuffer::new(width, height);

    render(&mut img, &scene, &camera, &samples_per_pixel, &max_depth);
    img.save("images/img.png").unwrap();
}

pub fn render(image: &mut ImageBuffer<Rgb<u8>, Vec<u8>>,
              scene: &HittableList,
              camera: &Camera,
              samples_per_pixel: &u32,
              max_depth: &u32
) {
    let (width, height) = image.dimensions();
    for (i, j, pixel) in image.enumerate_pixels_mut() {
        let mut color_pixel = Color::new(0.0, 0.0, 0.0);
        for _ in 0..*samples_per_pixel {
            let u = (i as f64 + get_random_double()) / (width - 1) as f64;
            let v = (j as f64 + get_random_double()) / (height - 1) as f64;
            let ray = camera.get_ray(u, v);
            color_pixel += cast_ray(&ray, scene, max_depth);
        }
        *pixel = get_color(&color_pixel, samples_per_pixel);
    }
}

fn get_color(color: &Color, samples_per_pixel: &u32) -> Rgb<u8> {
    let scale = 1.0 / *samples_per_pixel as f64;
    let r = (color.x * scale).sqrt();
    let g = (color.y * scale).sqrt();
    let b = (color.z * scale).sqrt();
    Rgb([(256.0 * clamp(r, 0.0, 0.999)) as u8,
        (256.0 * clamp(g, 0.0, 0.999)) as u8,
        (256.0 * clamp(b, 0.0, 0.999)) as u8])
}

fn clamp(x: f64, min: f64, max: f64) -> f64 {
    if x < min { return min; }
    if x > max { return max; }
    x
}

pub fn cast_ray(ray: &Ray, scene: &HittableList, depth: &u32) -> Color {
    if *depth == 0 {
        return Color::new(0.0, 0.0, 0.0);
    }
    match scene.hit(ray, 0.001, f64::MAX) {
        Some(rec) => {
            return match rec.material.scatter(ray, &rec) {
                Some((attenuation, scattered)) =>
                    attenuation * cast_ray(&scattered, scene, &(depth - 1)),
                None => Color::new(0.0, 0.0, 0.0)
            }
        }
        None => {
            let unit_dir = ray.dir.unit_vector();
            let t = 0.5 * (unit_dir.y + 1.0);
            (1.0 - t) * Color::new(1.0, 1.0, 1.0) + t * Color::new(0.5, 0.7, 1.0)
        }
    }
}
