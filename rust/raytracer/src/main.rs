use std::ops::{Add, Div, Mul, Neg, Sub};

use image::{ImageBuffer, Rgb};

#[derive(Debug, Copy, Clone)]
pub struct Vec3 {
    pub x: f32,
    pub y: f32,
    pub z: f32,
}

impl Vec3 {
    pub fn new(x: f32, y: f32, z: f32) -> Vec3 {
        Vec3 { x, y, z }
    }

    pub fn length(&self) -> f32 {
        (self.x * self.x + self.y * self.y + self.z * self.z).sqrt()
    }

    pub fn dot(&self, other: &Vec3) -> f32 {
        self.x * other.x + self.y * other.y + self.z * other.z
    }

    pub fn cross(&self, other: &Vec3) -> Vec3 {
        Vec3::new(
            self.y * other.z - self.z * other.y,
            self.z * other.x - self.x * other.z,
            self.x * other.y - self.y * other.x,
        )
    }

    pub fn unit_vector(&self) -> Vec3 {
        *self / self.length()
    }
}

impl Add for Vec3 {
    type Output = Vec3;

    fn add(self, other: Vec3) -> Vec3 {
        Vec3::new(self.x + other.x, self.y + other.y, self.z + other.z)
    }
}

impl Sub for Vec3 {
    type Output = Vec3;

    fn sub(self, other: Vec3) -> Vec3 {
        Vec3::new(self.x - other.x, self.y - other.y, self.z - other.z)
    }
}

impl Mul for Vec3 {
    type Output = Vec3;

    fn mul(self, other: Vec3) -> Vec3 {
        Vec3::new(self.x * other.x, self.y * other.y, self.z * other.z)
    }
}

impl Mul<f32> for Vec3 {
    type Output = Vec3;

    fn mul(self, other: f32) -> Vec3 {
        Vec3::new(self.x * other, self.y * other, self.z * other)
    }
}

impl Mul<Vec3> for f32 {
    type Output = Vec3;

    fn mul(self, other: Vec3) -> Vec3 {
        Vec3::new(self * other.x, self * other.y, self * other.z)
    }
}

impl Div<f32> for Vec3 {
    type Output = Vec3;

    fn div(self, other: f32) -> Vec3 {
        Vec3::new(self.x / other, self.y / other, self.z / other)
    }
}

impl Neg for Vec3 {
    type Output = Vec3;

    fn neg(self) -> Vec3 {
        Vec3::new(-self.x, -self.y, -self.z)
    }
}

#[derive(Debug, Copy, Clone)]
pub struct Ray {
    pub origin: Vec3,
    pub direction: Vec3,
}

impl Ray {
    pub fn new(origin: Vec3, direction: Vec3) -> Ray {
        Ray { origin, direction }
    }

    pub fn point_at_parameter(&self, t: f32) -> Vec3 {
        self.origin + t * self.direction
    }
}

#[derive(Debug, Copy, Clone)]
pub struct Sphere {
    pub center: Vec3,
    pub radius: f32,
}

fn sq(x: f32) -> f32 {
    f32::powf(x, 2.0)
}

impl Sphere {
    pub fn new(center: Vec3, radius: f32) -> Sphere {
        Sphere { center, radius }
    }

    pub fn render(&self, image: &mut ImageBuffer<Rgb<u8>, Vec<u8>>) {
        for (_j, _i, pixel) in image.enumerate_pixels_mut() {
            // let x_: f32 = (2 * (i + 0.5) / width as f32 - 1.0) * tan();
            let hit = self.hit(&ray, 0.0, 100.0);
            if hit {
                *pixel = Rgb([255, 255, 255]);
            }
        }
    }

    pub fn intersect(&self, ray: &Ray, &mut t0: f32) -> bool {
        let l = self.center - ray.origin;
        let tc = l.dot(&ray.direction);
        let d2 = l.dot(&l) - sq(tc);
        if d2 > sq(self.radius) {
            return false;
        }
        let thc = (sq(self.radius) - d2).sqrt();
        t0 = tc - thc;
        let t1 = tc + thc;
        if t0 < 0 as f32 {
            t0 = t1;
        }
        if t0 < 0 as f32 {
            return false;
        }
        return true;
    }
}

pub fn cast_ray(ray: &Ray, sphere: &Sphere) -> Vec3 {
    if !sphere.intersect(&ray, f32::MAX) {
        return Vec3::new(0.0, 0.0, 0.0);
    }
    return Vec3::new(1.0, 1.0, 1.0);
}

fn main() {
    let width = 800;
    let height = 800;

    let mut img = image::ImageBuffer::new(width, height);

    for (x, y, pixel) in img.enumerate_pixels_mut() {
        let r = (0.3 * x as f32) as u8;
        let b = (0.3 * y as f32) as u8;
        *pixel = Rgb([r, 0, b]);
    }

    let _sphere = Sphere::new(Vec3::new(400.0, 400.0, 0.0), 100.0);
    // sphere.render(&mut img);
    img.save("images/img.png").unwrap();
}